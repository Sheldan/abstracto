package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.DynamicKeyLoader;
import dev.sheldan.abstracto.core.exception.ConfigurationException;
import dev.sheldan.abstracto.core.management.EmoteManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.repository.EmoteRepository;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmoteManagementServiceBean implements EmoteManagementService {

    @Autowired
    private EmoteRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private DynamicKeyLoader dynamicKeyLoader;

    @Override
    public AEmote loadEmote(Long id) {
        return repository.getOne(id);
    }

    @Override
    public AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return this.createCustomEmote(name, emoteKey, emoteId, animated, server);
    }

    @Override
    public AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, AServer server) {
        validateEmoteName(name);
        AEmote emoteToCreate = AEmote
                .builder()
                .custom(true)
                .name(name)
                .animated(animated)
                .emoteId(emoteId)
                .emoteKey(emoteKey)
                .serverRef(server)
                .build();
        repository.save(emoteToCreate);
        return emoteToCreate;
    }

    @Override
    public AEmote createDefaultEmote(String name, String emoteKey, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return createDefaultEmote(name, emoteKey, server);
    }

    @Override
    public AEmote createDefaultEmote(String name, String emoteKey, AServer server) {
        validateEmoteName(name);
        AEmote emoteToCreate = AEmote
                .builder()
                .custom(false)
                .name(name)
                .emoteKey(emoteKey)
                .serverRef(server)
                .build();
        repository.save(emoteToCreate);
        return emoteToCreate;
    }

    @Override
    public Optional<AEmote> loadEmoteByName(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return loadEmoteByName(name, server);
    }

    @Override
    public Optional<AEmote> loadEmoteByName(String name, AServer server) {
        return Optional.ofNullable(repository.findAEmoteByNameAndServerRef(name, server));
    }

    @Override
    public AEmote setEmoteToCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AEmote emote;
        Optional<AEmote> emoteOptional = loadEmoteByName(name, server);
        if(!emoteOptional.isPresent()) {
            emote = this.createCustomEmote(name, emoteKey, emoteId, animated, server);
        } else {
            emote = emoteOptional.get();
            emote.setEmoteKey(emoteKey);
            emote.setEmoteId(emoteId);
            emote.setAnimated(animated);
            emote.setCustom(true);
            repository.save(emote);
        }
        return emote;
    }

    @Override
    public AEmote setEmoteToCustomEmote(String name, Emote emote, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AEmote emoteBeingSet;
        Optional<AEmote> emoteOptional = loadEmoteByName(name, serverId);
        if(!emoteOptional.isPresent()) {
            emoteBeingSet = this.createCustomEmote(name, emote.getName(), emote.getIdLong(), emote.isAnimated(), server);
        } else {
            emoteBeingSet = emoteOptional.get();
            emoteBeingSet.setCustom(true);
            emoteBeingSet.setEmoteId(emote.getIdLong());
            emoteBeingSet.setAnimated(emote.isAnimated());
            emoteBeingSet.setEmoteKey(emote.getName());
            repository.save(emoteBeingSet);
        }
        return emoteBeingSet;
    }

    @Override
    public AEmote setEmoteToDefaultEmote(String name, String emoteKey, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AEmote emoteBeingSet;
        Optional<AEmote> emoteOptional = loadEmoteByName(name, serverId);
        if(!emoteOptional.isPresent()) {
            emoteBeingSet = this.createDefaultEmote(name, emoteKey, server);
        } else {
            emoteBeingSet = emoteOptional.get();
            emoteBeingSet.setEmoteKey(emoteKey);
            emoteBeingSet.setCustom(false);
            repository.save(emoteBeingSet);
        }
        return emoteBeingSet;
    }

    @Override
    public boolean emoteExists(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return emoteExists(name, server);
    }

    @Override
    public boolean emoteExists(String name, AServer server) {
        return repository.existsByNameAndServerRef(name, server);
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

    private void validateEmoteName(String name) {
        List<String> possibleEmotes = dynamicKeyLoader.getEmoteNamesAsList();
        if(!possibleEmotes.contains(name)) {
            throw new ConfigurationException("Emote `" + name + "` is not defined. Possible values are: " + String.join(", ", possibleEmotes));
        }
    }
}
