package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.listener.ServerCreatedListenerModel;
import dev.sheldan.abstracto.core.repository.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ServerManagementServiceBean implements ServerManagementService {

    @Autowired
    private ServerRepository repository;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public AServer createServer(Long id) {
        AServer newServer = AServer.builder().id(id).adminMode(false).build();
        log.info("Creating server with id {}.", id);
        AServer server = repository.save(newServer);
        ServerCreatedListenerModel model = getModel(server);
        eventPublisher.publishEvent(model);
        return server;
    }

    private ServerCreatedListenerModel getModel(AServer server) {
        return ServerCreatedListenerModel
                .builder()
                .serverId(server.getId())
                .build();
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
        // we need to reload the user, because the user already got persisted within this transaction (if the user didnt exist)
        // but it seems the current transaction is not aware that it is already persisted, it tries to create again
        // but fails with integrity constraint violation
        userManagementService.loadOrCreateUser(userId);
        AUser user = userManagementService.loadUser(userId);
        AServer serverReference = loadServer(serverId);
        AUserInAServer aUserInAServer = AUserInAServer.builder().serverReference(serverReference).userReference(user).build();
        serverReference.getUsers().add(aUserInAServer);
        return aUserInAServer;
    }

    @Override
    public List<AServer> getAllServers() {
        return repository.findAll();
    }


}
