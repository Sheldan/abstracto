package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.repository.ServerRepository;
import lombok.extern.slf4j.Slf4j;
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
        return repository.save(AServer.builder().id(id).build());
    }

    @Override
    public AServer loadOrCreate(Long id) {
        if(repository.existsById(id)) {
            return repository.findById(id).get();
        } else {
            return createServer(id);
        }
    }

    @Override
    public void addChannelToServer(AServer server, AChannel channel) {
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
        Optional<AServer> server = repository.findById(serverId);
        AUser user = userManagementService.loadUser(userId);
        AServer serverReference = server.orElseThrow(() -> new AbstractoRunTimeException(String.format("Cannnot find server %s", serverId)));
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
