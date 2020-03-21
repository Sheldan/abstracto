package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.management.EmoteManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.repository.EmoteRepository;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmoteManagementServiceBean implements EmoteManagementService {

    @Autowired
    private EmoteRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public AEmote loadEmote(Long id) {
        return repository.getOne(id);
    }

    @Override
    public AEmote loadEmoteByName(String name, Long serverId) {
        AServer server = serverManagementService.loadServer(serverId);
        return repository.findAEmoteByNameAndServerRef(name, server);
    }

    @Override
    public AEmote setEmoteToCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId) {
        AEmote existing = loadEmoteByName(name, serverId);
        existing.setEmoteKey(emoteKey);
        existing.setEmoteId(emoteId);
        existing.setAnimated(animated);
        existing.setCustom(true);
        repository.save(existing);
        return existing;
    }

    @Override
    public AEmote setEmoteToCustomEmote(String name, Emote emote, Long serverId) {
        AEmote existing = loadEmoteByName(name, serverId);
        existing.setCustom(true);
        existing.setAnimated(emote.isAnimated());
        existing.setEmoteId(emote.getIdLong());
        existing.setEmoteKey(emote.getName());
        repository.save(existing);
        return existing;
    }

    @Override
    public AEmote setEmoteToDefaultEmote(String name, String emoteKey, Long serverId) {
        AEmote existing = loadEmoteByName(name, serverId);
        existing.setEmoteKey(emoteKey);
        existing.setCustom(false);
        repository.save(existing);
        return existing;
    }

    @Override
    public AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated) {
        AEmote emote = AEmote.builder()
                .animated(animated)
                .custom(true)
                .emoteKey(emoteKey)
                .emoteId(emoteId)
                .name(name)
                .build();
        repository.save(emote);
        return emote;
    }

    @Override
    public AEmote createDefaultEmote(String name, String emoteKey) {
        AEmote emote = AEmote.builder()
                .custom(false)
                .emoteKey(emoteKey)
                .name(name)
                .build();
        repository.save(emote);
        return emote;
    }
}
