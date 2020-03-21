package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.database.AEmote;

public interface EmoteManagementService {
    AEmote loadEmote(Long id);
    AEmote loadEmoteByName(String name, Long serverId);
    AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated);
    AEmote createDefaultEmote(String name, String emoteKey);
}
