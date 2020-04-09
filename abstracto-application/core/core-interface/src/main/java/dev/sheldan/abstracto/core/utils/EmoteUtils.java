package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.models.dto.EmoteDto;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.Optional;

public class EmoteUtils {

    public static boolean isReactionEmoteAEmote(MessageReaction.ReactionEmote reaction, EmoteDto emote, Emote emoteInGuild) {
        if(reaction.isEmote() && emote.getCustom()) {
            if(emoteInGuild != null) {
                return emoteInGuild.equals(reaction.getEmote());
            } else {
                return false;
            }
        } else {
            return reaction.getEmoji().equals(emote.getEmoteKey());
        }
    }

    public static Optional<CachedReaction> getReactionFromMessageByEmote(CachedMessage message, EmoteDto emote) {
        return message.getReactions().stream().filter(reaction -> compareAEmote(reaction.getEmote(), emote)).findFirst();
    }

    public static boolean compareAEmote(EmoteDto a, EmoteDto b) {
        if(a.getCustom() && b.getCustom()) {
            return a.getEmoteId().equals(b.getEmoteId());
        } else {
            if(!a.getCustom() && !b.getCustom()) {
                return a.getEmoteKey().equals(b.getEmoteKey());
            } else {
                return false;
            }
        }
    }

}
