package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

import java.util.Optional;

public interface EmoteManagementService {
    Optional<AEmote> loadEmoteOptional(Integer id);
    AEmote loadEmote(Integer id);
    AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId, boolean validateName);
    AEmote createCustomEmote(String name, AEmote fakeEmote, Long serverId, boolean validateName);
    AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, AServer server, boolean validateName);
    AEmote createDefaultEmote(String name, String emoteKey, Long serverId, boolean validateName);
    AEmote createDefaultEmote(String name, String emoteKey, AServer server, boolean validateName);
    Optional<AEmote> loadEmoteByName(String name, Long serverId);
    Optional<AEmote> loadEmoteByName(String name, AServer server);
    AEmote setEmoteToCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId);
    AEmote setEmoteToCustomEmote(String name, CustomEmoji emote, Long serverId);
    AEmote setEmoteToDefaultEmote(String name, String emoteKey, Long serverId);
    AEmote setEmoteToAEmote(String name, AEmote emote, Long serverId);
    AEmote createEmote(String name, AEmote emote, Long serverId, boolean validateName);
    boolean emoteExists(String name, Long serverId);
    boolean emoteExists(Long emoteId);
    void deleteEmote(AEmote aEmote);
    Optional<AEmote> loadEmote(Long id);
    boolean emoteExists(String name, AServer server);
}
