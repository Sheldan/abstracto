package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.*;

import java.util.List;
import java.util.Optional;

public interface ServerManagementService {
    AServer createServer(Long id);
    AServer loadOrCreate(Long id);
    AServer loadServer(Long id);
    Optional<AServer> loadServerOptional(Long id);
    void addChannelToServer(AServer server, AChannel channel);
    AUserInAServer addUserToServer(AServer server, AUser user);
    AUserInAServer addUserToServer(Long serverId, Long userId);
    AChannel getPostTarget(Long serverId, String name);
    AChannel getPostTarget(Long serverId, PostTarget target);
    AChannel getPostTarget(AServer server, PostTarget target);
    AChannel getPostTarget(AServer server, String name);
    List<AServer> getAllServers();
}
