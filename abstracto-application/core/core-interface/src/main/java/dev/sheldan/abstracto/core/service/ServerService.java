package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;

public interface ServerService {
    AServer createServer(Long id);
    void addChannelToServer(AServer server, AChannel channel);
}
