package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.repository.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ServerManagementServiceBean implements ServerManagementService {

    @Autowired
    private ServerRepository repository;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private UserManagementService userManagementService;

    @Override
    public AServer createServer(Long id) {
        AServer newServer = AServer.builder().id(id).build();
        log.info("Creating server with id {}.", id);
        return repository.save(newServer);
    }

    @Override
    public AServer loadOrCreate(Long id) {
        Optional<AServer> optional = repository.findById(id);
        return optional.orElseGet(() -> createServer(id));
    }

    @Override
    public AServer loadServer(Long id) {
        return loadServerOptional(id).orElseThrow(() -> new GuildNotFoundException(id));
    }

    @Override
    public AServer loadServer(Guild guild) {
        return loadServer(guild.getIdLong());
    }

    @Override
    public Optional<AServer> loadServerOptional(Long id) {
        return repository.findById(id);
    }

    @Override
    public void addChannelToServer(AServer server, AChannel channel) {
        log.info("Adding channel {} to server {}.", channel.getId(), server.getId());
        server.getChannels().add(channel);
        channel.setServer(server);
    }

    @Override
    public AUserInAServer addUserToServer(AServer server, AUser user) {
        return this.addUserToServer(server.getId(), user.getId());
    }

    @Override
    public AUserInAServer addUserToServer(Long serverId, Long userId) {
        log.info("Adding user {} to server {}", userId, serverId);
        AUser user = userManagementService.loadUser(userId);
        AServer serverReference = loadServer(serverId);
        AUserInAServer aUserInAServer = AUserInAServer.builder().serverReference(serverReference).userReference(user).build();
        serverReference.getUsers().add(aUserInAServer);
        return aUserInAServer;
    }

    @Override
    public AChannel getPostTarget(Long serverId, String name) {
        AServer server = this.loadOrCreate(serverId);
        return getPostTarget(server, name);
    }

    @Override
    public AChannel getPostTarget(Long serverId, PostTarget target) {
        AServer server = this.loadOrCreate(serverId);
        return getPostTarget(server, target);
    }

    @Override
    public AChannel getPostTarget(AServer server, PostTarget target) {
        return target.getChannelReference();
    }

    @Override
    public AChannel getPostTarget(AServer server, String name) {
        PostTarget target = postTargetManagement.getPostTarget(name, server);
        return getPostTarget(server, target);
    }

    @Override
    public List<AServer> getAllServers() {
        return repository.findAll();
    }


}
