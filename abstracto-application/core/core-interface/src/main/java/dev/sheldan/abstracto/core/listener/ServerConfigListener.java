package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.dto.ServerDto;

public interface ServerConfigListener {
    void updateServerConfig(ServerDto server);
}
