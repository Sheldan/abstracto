package dev.sheldan.abstracto.core.listener.sync.entity;

import dev.sheldan.abstracto.core.models.database.AServer;

public interface ServerConfigListener {
    void updateServerConfig(AServer server);
}
