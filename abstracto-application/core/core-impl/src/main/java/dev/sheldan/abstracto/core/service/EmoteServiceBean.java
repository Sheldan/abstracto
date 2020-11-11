package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.EmoteNotDefinedException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.DefaultEmoteManagementService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class EmoteServiceBean implements EmoteService {

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private DefaultEmoteManagementService defaultEmoteManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public boolean isEmoteUsableByBot(Emote emote) {
        for (Guild guild : botService.getInstance().getGuilds()) {
            Emote emoteById = guild.getEmoteById(emote.getId());
            if(emoteById != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AEmote buildAEmoteFromReaction(MessageReaction.ReactionEmote reaction) {
        if(reaction.isEmote()) {
            return AEmote.builder().emoteKey(reaction.getName()).custom(true).emoteId(reaction.getEmote().getIdLong()).animated(reaction.getEmote().isAnimated()).build();
        } else {
            return AEmote.builder().emoteKey(reaction.getEmoji()).custom(false).build();
        }
    }

    @Override
    public String getEmoteAsMention(AEmote emote, Long serverId, String defaultText)  {
        if(emote != null && emote.getCustom()) {
            Optional<Emote> emoteOptional = botService.getEmote(serverId, emote);
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
    public boolean isReactionEmoteAEmote(MessageReaction.ReactionEmote reaction, AEmote storedEmote) {
        if(reaction.isEmote() && storedEmote.getCustom()) {
            return reaction.getEmote().getIdLong() == storedEmote.getEmoteId();
        } else if(reaction.isEmoji()){
            return reaction.getEmoji().equals(storedEmote.getEmoteKey());
        }
        return false;
    }

    @Override
    public Optional<CachedReaction> getReactionFromMessageByEmote(CachedMessage message, AEmote emote) {
        return message.getReactions().stream().filter(reaction -> compareAEmote(reaction.getEmote(), emote)).findFirst();
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
    public AEmote getFakeEmote(Object object) {
        if(object instanceof Emote) {
            Emote emote = (Emote) object;
            return getFakeEmoteFromEmote(emote);
        } else if(object instanceof String) {
            String emoteText = (String) object;
            return AEmote.builder().fake(true).custom(false).emoteKey(emoteText).build();
        }
        throw new IllegalArgumentException("Not possible to convert given object to AEmote.");
    }

    @Override
    public AEmote getFakeEmoteFromEmote(Emote emote) {
        AServer server = null;
        if(emote.getGuild() != null) {
            server = AServer.builder().id(emote.getGuild().getIdLong()).fake(true).build();
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
    public boolean emoteIsFromGuild(Emote emote, Guild guild) {
        return guild.getEmoteById(emote.getId()) != null;
    }

}
