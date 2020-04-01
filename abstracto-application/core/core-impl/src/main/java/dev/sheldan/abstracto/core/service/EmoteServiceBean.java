package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AEmote;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmoteServiceBean implements EmoteService {

    @Autowired
    private Bot botService;

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
    public String getEmoteAsMention(AEmote emote, Long serverId, String defaultText) {
        if(emote != null && emote.getCustom()) {
            Emote emote1 = botService.getEmote(serverId, emote);
            if (emote1 != null) {
                return emote1.getAsMention();
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
    public String getEmoteAsMention(AEmote emote, Long serverId) {
        return this.getEmoteAsMention(emote, serverId, " ");
    }
}
