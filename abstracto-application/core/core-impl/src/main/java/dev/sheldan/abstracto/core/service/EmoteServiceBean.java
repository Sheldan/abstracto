package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.EmoteNotDefinedException;
import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.DefaultEmoteManagementService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class EmoteServiceBean implements EmoteService {

    @Autowired
    private BotService botService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private DefaultEmoteManagementService defaultEmoteManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public boolean isEmoteUsableByBot(CustomEmoji emote) {
        for (Guild guild : botService.getInstance().getGuilds()) {
            CustomEmoji emoteById = guild.getEmojiById(emote.getId());
            if(emoteById != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AEmote buildAEmoteFromReaction(Emoji reaction) {
        if(reaction.getType().equals(Emoji.Type.CUSTOM)) {
            CustomEmoji CustomEmoji = (CustomEmoji) reaction;
            return AEmote.builder().emoteKey(reaction.getName()).custom(true).emoteId(CustomEmoji.getIdLong()).animated(CustomEmoji.isAnimated()).build();
        } else {
            return AEmote.builder().emoteKey(reaction.getName()).custom(false).build();
        }
    }

    @Override
    public String getEmoteAsMention(AEmote emote, Long serverId, String defaultText)  {
        if(emote != null && emote.getCustom()) {
            Optional<CustomEmoji> emoteOptional = getEmote(serverId, emote);
            if (emoteOptional.isPresent()) {
                return emoteOptional.get().getAsMention();
            } else {
                log.warn("Emote {} with name {} in server {} defined, but not usable.", emote.getEmoteId(), emote.getName(), serverId);
                return defaultText;
            }
        } else {
            if(emote == null) {
                return defaultText;
            }
            return emote.getEmoteKey();
        }
    }

    @Override
    public String getEmoteAsMention(AEmote emote, Long serverId)  {
        return this.getEmoteAsMention(emote, serverId, " ");
    }

    @Override
    public String getUsableEmoteOrDefault(Long serverId, String name) {
        Optional<AEmote> aEmote = emoteManagementService.loadEmoteByName(name, serverId);
        String defaultEmote = getDefaultEmote(name);
        return getEmoteAsMention(aEmote.orElse(null), serverId, defaultEmote);
    }

    @Override
    public void throwIfEmoteDoesNotExist(String emoteKey, Long serverId)  {
        if(!emoteManagementService.loadEmoteByName(emoteKey, serverId).isPresent()) {
            throw new EmoteNotDefinedException(emoteKey);
        }
    }

    @Override
    public AEmote getEmoteOrDefaultEmote(String emoteKey, Long serverId) {
        Optional<AEmote> emoteOptional = emoteManagementService.loadEmoteByName(emoteKey, serverId);
        return emoteOptional.orElseGet(() -> AEmote.builder().emoteKey(getDefaultEmote(emoteKey)).custom(false).name(emoteKey).build());
    }

    @Override
    public String getDefaultEmote(String emoteKey) {
        return defaultEmoteManagementService.getDefaultEmote(emoteKey).getName();
    }

    @Override
    public boolean isReactionEmoteAEmote(Emoji reaction, AEmote storedEmote) {
        if(reaction.getType().equals(Emoji.Type.CUSTOM) && storedEmote.getCustom() && reaction instanceof CustomEmoji) {
            CustomEmoji emoji = (CustomEmoji) reaction;
            return emoji.getIdLong() == storedEmote.getEmoteId();
        } else if(reaction.getType().equals(Emoji.Type.UNICODE)){
            return reaction.getName().equals(storedEmote.getEmoteKey());
        }
        return false;
    }

    @Override
    public Optional<CachedReactions> getReactionFromMessageByEmote(CachedMessage message, AEmote emote) {
        return message
                .getReactions()
                .stream()
                .filter(reaction -> compareCachedEmoteWithAEmote(reaction.getEmote(), emote))
                .findFirst();
    }

    @Override
    public boolean compareAEmote(AEmote a, AEmote b) {
        if(Boolean.TRUE.equals(a.getCustom()) && Boolean.TRUE.equals(b.getCustom())) {
            return a.getEmoteId().equals(b.getEmoteId());
        } else {
            if(Boolean.FALSE.equals(a.getCustom()) && Boolean.FALSE.equals(b.getCustom())) {
                return a.getEmoteKey().equals(b.getEmoteKey());
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean compareCachedEmoteWithAEmote(CachedEmote a, AEmote b) {
        if(Boolean.TRUE.equals(a.getCustom()) && Boolean.TRUE.equals(b.getCustom())) {
            return a.getEmoteId().equals(b.getEmoteId());
        } else {
            if(Boolean.FALSE.equals(a.getCustom()) && Boolean.FALSE.equals(b.getCustom())) {
                return a.getEmoteName().equals(b.getEmoteKey());
            } else {
                return false;
            }
        }
    }

    @Override
    public AEmote getFakeEmote(Object object) {
        if(object instanceof CustomEmoji) {
            CustomEmoji emote = (CustomEmoji) object;
            return getFakeEmoteFromEmote(emote);
        } else if(object instanceof String) {
            String emoteText = (String) object;
            return AEmote
                    .builder()
                    .fake(true)
                    .custom(false)
                    .animated(false)
                    .emoteKey(emoteText)
                    .build();
        }
        throw new IllegalArgumentException("Not possible to convert given object to AEmote.");
    }

    @Override
    public AEmote getFakeEmoteFromEmote(CustomEmoji emote) {
        AServer server = null;
        if(emote instanceof RichCustomEmoji) {
            RichCustomEmoji richCustomEmoji = (RichCustomEmoji) emote;
            server = AServer
                    .builder()
                    .id(richCustomEmoji.getGuild().getIdLong())
                    .fake(true)
                    .build();
        }
        return AEmote
                .builder()
                .fake(true)
                .emoteKey(emote.getName())
                .custom(true)
                .animated(emote.isAnimated())
                .emoteId(emote.getIdLong())
                .serverRef(server)
                .build();
    }

    @Override
    public AEmote getFakeEmoteFromEmoji(Emoji emoji) {
        if(emoji instanceof CustomEmoji) {
            return getFakeEmoteFromEmote((CustomEmoji) emoji);
        } else {
            return AEmote
                    .builder()
                    .fake(true)
                    .emoteKey(emoji.getName())
                    .custom(false)
                    .build();
        }
    }

    @Override
    public boolean emoteIsFromGuild(CustomEmoji emote, Guild guild) {
        return guild.getEmojiById(emote.getId()) != null;
    }

    @Override
    public CompletableFuture<CustomEmoji> getEmoteFromCachedEmote(CachedEmote cachedEmote) {
        if(!cachedEmote.getCustom()) {
            throw new IllegalArgumentException("Given Emote was not a custom emote.");
        }
        Guild guild = guildService.getGuildById(cachedEmote.getServerId());
        return guild.retrieveEmojiById(cachedEmote.getEmoteId()).submit().thenApply(listedEmote -> listedEmote);
    }

    @Override
    public Optional<CustomEmoji> getEmote(Long serverId, AEmote emote)  {
        if(Boolean.FALSE.equals(emote.getCustom())) {
            return Optional.empty();
        }
        Optional<Guild> guildById = guildService.getGuildByIdOptional(serverId);
        if(guildById.isPresent()) {
            Guild guild = guildById.get();
            CustomEmoji emoteById = guild.getEmojiById(emote.getEmoteId());
            return Optional.ofNullable(emoteById);
        }
        throw new GuildNotFoundException(serverId);
    }

    @Override
    public Optional<Emoji> getEmote(AEmote emote) {
        if(Boolean.FALSE.equals(emote.getCustom())) {
            return Optional.empty();
        }
        return Optional.ofNullable(botService.getInstance().getEmojiById(emote.getEmoteId()));
    }

}
