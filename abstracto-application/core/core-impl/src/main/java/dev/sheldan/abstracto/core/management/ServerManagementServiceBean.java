package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.PostTargetManagement;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServerManagementServiceBean implements ServerManagementService {

    @Autowired
    private ServerRepository repository;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public AServer createServer(Long id) {
        return repository.save(AServer.builder().id(id).build());
    }

    @Override
    public AServer loadServer(Long id) {
        return repository.getOne(id);
    }

    @Override
    public void addChannelToServer(AServer server, AChannel channel) {
        server.getChannels().add(channel);
    }

    @Override
    public AChannel getPostTarget(Long serverId, String name) {
        AServer server = this.loadServer(serverId);
        return getPostTarget(server, name);
    }

    @Override
    public AChannel getPostTarget(Long serverId, PostTarget target) {
        AServer server = this.loadServer(serverId);
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


}
