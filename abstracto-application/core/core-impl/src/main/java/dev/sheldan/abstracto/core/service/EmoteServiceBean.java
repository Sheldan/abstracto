package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.DynamicKeyLoader;
import dev.sheldan.abstracto.core.exception.EmoteException;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
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
    private Bot botService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private DynamicKeyLoader keyLoader;

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
            throw new EmoteException(String.format("Emote %s not defined.", emoteKey));
        }
    }

    @Override
    public AEmote getEmoteOrFakeEmote(String emoteKey, Long serverId) {
        Optional<AEmote> emoteOptional = emoteManagementService.loadEmoteByName(emoteKey, serverId);
        return emoteOptional.orElseGet(() -> AEmote.builder().emoteKey(getDefaultEmote(emoteKey)).custom(false).name(emoteKey).build());
    }

    @Override
    public String getDefaultEmote(String emoteKey) {
        return keyLoader.getDefaultEmotes().get(emoteKey);
    }
}
