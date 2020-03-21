package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.database.AEmote;
import net.dv8tion.jda.api.entities.Emote;

public interface EmoteManagementService {
    AEmote loadEmote(Long id);
    AEmote loadEmoteByName(String name, Long serverId);
    AEmote setEmoteToCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId);
    AEmote setEmoteToCustomEmote(String name, Emote emote, Long serverId);
    AEmote setEmoteToDefaultEmote(String name, String emoteKey, Long serverId);
    AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated);
    AEmote createDefaultEmote(String name, String emoteKey);
}
