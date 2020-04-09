package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.dto.EmoteDto;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;

import java.util.Optional;

public interface EmoteService {
    boolean isEmoteUsableByBot(Emote emote);
    EmoteDto buildAEmoteFromReaction(MessageReaction.ReactionEmote reaction);
    Optional<EmoteDto> getEmoteByName(String name, Long serverId);
    String getEmoteAsMention(EmoteDto emote, Long serverId, String defaultText) ;
    String getEmoteAsMention(EmoteDto emote, Long serverId) ;
    void throwIfEmoteDoesNotExist(String emoteKey, Long serverId) ;
}
