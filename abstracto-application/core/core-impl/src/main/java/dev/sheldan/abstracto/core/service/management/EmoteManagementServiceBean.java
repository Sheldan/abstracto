package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.EmoteNotFoundException;
import dev.sheldan.abstracto.core.exception.EmoteNotFoundInDbException;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.EmoteRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class EmoteManagementServiceBean implements EmoteManagementService {

    @Autowired
    private EmoteRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private DefaultEmoteManagementService defaultEmoteManagementService;

    @Override
    public Optional<AEmote> loadEmoteOptional(Integer id) {
        return repository.findById(id);
    }

    @Override
    public AEmote loadEmote(Integer id) {
        return loadEmoteOptional(id).orElseThrow(() -> new EmoteNotFoundInDbException(id));
    }

    @Override
    public Optional<AEmote> loadEmote(Long id) {
        return repository.findByEmoteId(id);
    }

    @Override
    public AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId, boolean validateName)  {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return this.createCustomEmote(name, emoteKey, emoteId, animated, server, validateName);
    }

    @Override
    public AEmote createCustomEmote(String name, AEmote fakeEmote, Long serverId, boolean validateName) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return this.createCustomEmote(name, fakeEmote.getEmoteKey(), fakeEmote.getEmoteId(), fakeEmote.getAnimated(), server, validateName);
    }

    @Override
    public AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, AServer server, boolean validateName)  {
        if(validateName) {
            validateEmoteName(name);
        }
        log.info("Creating custom emote: id {}, animated {}, in server {}.", emoteId, animated, server.getId());
        AEmote emoteToCreate = AEmote
                .builder()
                .custom(true)
                .changeable(true)
                .name(name)
                .animated(animated)
                .emoteId(emoteId)
                .emoteKey(emoteKey)
                .serverRef(server)
                .build();
        return repository.save(emoteToCreate);
    }

    @Override
    public AEmote createDefaultEmote(String name, String emoteKey, Long serverId, boolean validateName)  {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return createDefaultEmote(name, emoteKey, server, validateName);
    }

    @Override
    public AEmote createDefaultEmote(String name, String emoteKey, AServer server, boolean validateName)  {
        if(validateName) {
            validateEmoteName(name);
        }
        log.info("Creating default inbuilt emote {} in server {}.", emoteKey, server.getId());
        AEmote emoteToCreate = AEmote
                .builder()
                .custom(false)
                .changeable(true)
                .name(name)
                .emoteKey(emoteKey)
                .serverRef(server)
                .build();
        return repository.save(emoteToCreate);
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
    public AEmote setEmoteToCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId)  {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AEmote emote;
        Optional<AEmote> emoteOptional = loadEmoteByName(name, server);
        if(!emoteOptional.isPresent()) {
            emote = this.createCustomEmote(name, emoteKey, emoteId, animated, server, true);
        } else {
            emote = emoteOptional.get();
            log.debug("Setting existing emote (a: {}, c: {}, id: {}, discord id: {}) to new custom emote configuration: new id {}, animated {}.",
                    emote.getAnimated(), emote.getCustom(), emote.getId(), emote.getEmoteId(), emoteId, animated);
            emote.setEmoteKey(emoteKey);
            emote.setEmoteId(emoteId);
            emote.setAnimated(animated);
            emote.setCustom(true);
            return repository.save(emote);
        }
        return emote;
    }

    @Override
    public AEmote setEmoteToCustomEmote(String name, CustomEmoji emote, Long serverId)  {
        return setEmoteToCustomEmote(name, emote.getName(), emote.getIdLong(), emote.isAnimated(), serverId);
    }

    @Override
    public AEmote setEmoteToDefaultEmote(String name, String emoteKey, Long serverId)  {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AEmote emoteBeingSet;
        Optional<AEmote> emoteOptional = loadEmoteByName(name, serverId);
        if(!emoteOptional.isPresent()) {
            emoteBeingSet = this.createDefaultEmote(name, emoteKey, server, true);
        } else {
            emoteBeingSet = emoteOptional.get();
            log.debug("Setting existing emote (a: {}, c: {}, id: {}, discord id: {}) to new default emote {}.",
                    emoteBeingSet.getAnimated(), emoteBeingSet.getCustom(), emoteBeingSet.getId(), emoteBeingSet.getEmoteId(), emoteKey);
            emoteBeingSet.setEmoteKey(emoteKey);
            emoteBeingSet.setCustom(false);
            emoteBeingSet.setAnimated(false);
            return repository.save(emoteBeingSet);
        }
        return emoteBeingSet;
    }

    @Override
    public AEmote setEmoteToAEmote(String name, AEmote fakeEmote, Long serverId) {
        Optional<AEmote> emoteOptional = loadEmoteByName(name, serverId);
        if(!emoteOptional.isPresent()) {
            return createEmote(name, fakeEmote, serverId, true);
        } else {
            AEmote emoteBeingSet = emoteOptional.get();
            if(fakeEmote.getCustom()) {
                log.debug("Setting existing emote (a: {}, c: {}, id: {}, discord id: {}) to new custom emote configuration: new id {}, animated {}.",
                        emoteBeingSet.getAnimated(), emoteBeingSet.getCustom(), emoteBeingSet.getId(), emoteBeingSet.getEmoteId(), fakeEmote.getEmoteId(), fakeEmote.getAnimated());
                emoteBeingSet.setEmoteId(fakeEmote.getEmoteId());
                emoteBeingSet.setAnimated(fakeEmote.getAnimated());
            } else {
                log.debug("Setting existing emote (a: {}, c: {}, id: {}, discord id: {}) to new default emote {}.",
                        emoteBeingSet.getAnimated(), emoteBeingSet.getCustom(), emoteBeingSet.getId(), emoteBeingSet.getEmoteId(), fakeEmote.getEmoteKey());
                emoteBeingSet.setEmoteId(null);
                emoteBeingSet.setAnimated(false);
            }
            emoteBeingSet.setCustom(fakeEmote.getCustom());
            emoteBeingSet.setEmoteKey(fakeEmote.getEmoteKey());
            return emoteBeingSet;
        }
    }

    @Override
    public AEmote createEmote(String name, AEmote fakeEmote, Long serverId, boolean validateName) {
        if(fakeEmote.getCustom()) {
            return this.createCustomEmote(name, fakeEmote, serverId, validateName);
        } else {
            return this.createDefaultEmote(name, fakeEmote.getEmoteKey(), serverId, validateName);
        }
    }

    @Override
    public boolean emoteExists(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return emoteExists(name, server);
    }

    @Override
    public boolean emoteExists(Long emoteId) {
        return repository.existsByEmoteId(emoteId);
    }

    @Override
    public void deleteEmote(AEmote aEmote) {
        log.info("Deleting emote with id {}", aEmote.getId());
        repository.delete(aEmote);
    }

    @Override
    public boolean emoteExists(String name, AServer server) {
        return repository.existsByNameAndServerRef(name, server);
    }

    private void validateEmoteName(String name)  {
        List<String> possibleEmotes = defaultEmoteManagementService.getDefaultEmoteNames();
        if(!possibleEmotes.contains(name)) {
            throw new EmoteNotFoundException(name, possibleEmotes);
        }
    }
}
