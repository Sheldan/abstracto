package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.Optional;

public class EmoteUtils {

    private EmoteUtils() {

    }

    public static boolean isReactionEmoteAEmote(MessageReaction.ReactionEmote reaction, AEmote emote, Emote emoteInGuild) {
        if(reaction.isEmote() && emote.getCustom()) {
            if(emoteInGuild != null) {
                return emoteInGuild.equals(reaction.getEmote());
            } else {
                return false;
            }
        } else if(reaction.isEmoji()){
            return reaction.getEmoji().equals(emote.getEmoteKey());
        }
        return false;
    }

    public static Optional<CachedReaction> getReactionFromMessageByEmote(CachedMessage message, AEmote emote) {
        return message.getReactions().stream().filter(reaction -> compareAEmote(reaction.getEmote(), emote)).findFirst();
    }

    public static boolean compareAEmote(AEmote a, AEmote b) {
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
