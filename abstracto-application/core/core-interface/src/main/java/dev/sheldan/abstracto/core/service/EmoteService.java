package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;

public interface EmoteService {
    boolean isEmoteUsableByBot(Emote emote);
    AEmote buildAEmoteFromReaction(MessageReaction.ReactionEmote reaction);
    String getEmoteAsMention(AEmote emote, Long serverId, String defaultText) ;
    String getEmoteAsMention(AEmote emote, Long serverId) ;
    void throwIfEmoteDoesNotExist(String emoteKey, Long serverId) ;
}
