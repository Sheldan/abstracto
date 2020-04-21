package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StarboardConfigListener implements ServerConfigListener {

    @Autowired
    private StarboardConfig starboardConfig;

    @Autowired
    private ConfigManagementService configManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Creating starboard config for server {}", server.getId());
        for (int i = 0; i < starboardConfig.getLvl().size(); i++) {
            Integer value = starboardConfig.getLvl().get(i);
            configManagementService.createIfNotExists(server.getId(), "starLvl" + ( i + 1 ), Double.valueOf(value));
        }
    }
}
