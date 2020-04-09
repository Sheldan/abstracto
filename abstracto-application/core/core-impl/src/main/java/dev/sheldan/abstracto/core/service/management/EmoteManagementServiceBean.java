package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.DynamicKeyLoader;
import dev.sheldan.abstracto.core.exception.EmoteException;
import dev.sheldan.abstracto.core.models.AEmote;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.converter.EmoteConverter;
import dev.sheldan.abstracto.core.models.converter.ServerConverter;
import dev.sheldan.abstracto.core.models.dto.EmoteDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.repository.EmoteRepository;
import net.dv8tion.jda.api.entities.Emote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmoteManagementServiceBean {

    @Autowired
    private EmoteRepository repository;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    @Autowired
    private DynamicKeyLoader dynamicKeyLoader;

    @Autowired
    private Bot botService;

    @Autowired
    private ServerConverter serverConverter;

    @Autowired
    private EmoteConverter emoteConverter;

    public AEmote loadEmote(Long id) {
        return repository.getOne(id);
    }

    public AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId)  {
        ServerDto server = ServerDto.builder().id(serverId).build();
        return this.createCustomEmote(name, emoteKey, emoteId, animated, server);
    }

    public AEmote createCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, ServerDto server)  {
        AServer aServer = serverConverter.fromDto(server);
        validateEmoteName(name);
        AEmote emoteToCreate = AEmote
                .builder()
                .custom(true)
                .name(name)
                .animated(animated)
                .emoteId(emoteId)
                .emoteKey(emoteKey)
                .serverRef(aServer)
                .build();
        repository.save(emoteToCreate);
        return emoteToCreate;
    }

    public AEmote createDefaultEmote(String name, String emoteKey, Long serverId)  {
        ServerDto server = ServerDto.builder().id(serverId).build();
        return createDefaultEmote(name, emoteKey, server);
    }

    public AEmote createDefaultEmote(String name, String emoteKey, ServerDto server)  {
        validateEmoteName(name);
        AServer aServer = AServer.builder().id(server.getId()).build();
        AEmote emoteToCreate = AEmote
                .builder()
                .custom(false)
                .name(name)
                .emoteKey(emoteKey)
                .serverRef(aServer)
                .build();
        repository.save(emoteToCreate);
        return emoteToCreate;
    }

    public Optional<AEmote> loadEmoteByName(String name, Long serverId) {
        ServerDto server = ServerDto.builder().id(serverId).build();
        return loadEmoteByName(name, server);
    }

    public Optional<AEmote> loadEmoteByName(String name, ServerDto server) {
        return Optional.ofNullable(repository.findAEmoteByNameAndServerRef(name, serverConverter.fromDto(server)));
    }

    public AEmote setEmoteToCustomEmote(String name, String emoteKey, Long emoteId, Boolean animated, Long serverId)  {
        ServerDto server = ServerDto.builder().id(serverId).build();
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

    public AEmote setEmoteToCustomEmote(String name, Emote emote, Long serverId)  {
        ServerDto server = ServerDto.builder().id(serverId).build();
        AEmote emoteBeingSet;
        Optional<AEmote> emoteOptional = loadEmoteByName(name, server);
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

    public EmoteDto setEmoteToDefaultEmote(String name, String emoteKey, Long serverId)  {
        ServerDto server = ServerDto.builder().id(serverId).build();
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
        return emoteConverter.fromEmote(emoteBeingSet);
    }

    public boolean emoteExists(String name, Long serverId) {
        ServerDto serverDto = ServerDto.builder().id(serverId).build();
        return emoteExists(name, serverDto);
    }

    public boolean emoteExists(String name, ServerDto server) {
        AServer server1 = AServer.builder().id(server.getId()).build();
        return repository.existsByNameAndServerRef(name, server1);
    }

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

    public AEmote createDefaultEmote(String name, String emoteKey) {
        AEmote emote = AEmote.builder()
                .custom(false)
                .emoteKey(emoteKey)
                .name(name)
                .build();
        repository.save(emote);
        return emote;
    }

    private void validateEmoteName(String name)  {
        List<String> possibleEmotes = dynamicKeyLoader.getEmoteNamesAsList();
        if(!possibleEmotes.contains(name)) {
            throw new EmoteException("Emote `" + name + "` is not defined. Possible values are: " + String.join(", ", possibleEmotes));
        }
    }
}
