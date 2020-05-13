package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import net.dv8tion.jda.api.entities.Emote;

import java.util.Optional;

public interface EmoteManagementService {
    Optional<AEmote> loadEmote(Long id);
    AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId) ;
    AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, AServer server) ;
    AEmote createDefaultEmote(String name, String emoteKey, Long serverId) ;
    AEmote createDefaultEmote(String name, String emoteKey, AServer server) ;
    Optional<AEmote> loadEmoteByName(String name, Long serverId);
    Optional<AEmote> loadEmoteByName(String name, AServer server);
    AEmote setEmoteToCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId) ;
    AEmote setEmoteToCustomEmote(String name, Emote emote, Long serverId) ;
    AEmote setEmoteToDefaultEmote(String name, String emoteKey, Long serverId) ;
    boolean emoteExists(String name, Long serverId);
    boolean emoteExists(String name, AServer server);
}
