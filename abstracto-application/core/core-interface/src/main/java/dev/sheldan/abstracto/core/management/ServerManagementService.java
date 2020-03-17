package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.PostTarget;

public interface ServerManagementService {
    AServer createServer(Long id);
    AServer loadServer(Long id);
    void addChannelToServer(AServer server, AChannel channel);
    AChannel getPostTarget(Long serverId, String name);
    AChannel getPostTarget(Long serverId, PostTarget target);
    AChannel getPostTarget(AServer server, PostTarget target);
    AChannel getPostTarget(AServer server, String name);
}
