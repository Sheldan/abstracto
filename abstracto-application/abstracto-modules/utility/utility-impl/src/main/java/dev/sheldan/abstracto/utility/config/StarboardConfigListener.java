package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.management.ConfigManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class StarboardConfigListener implements ServerConfigListener {

    @Autowired
    private StarboardConfig starboardConfig;

    @Autowired
    private ConfigManagementService configManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        for (int i = 0; i < starboardConfig.getLvl().size(); i++) {
            Integer value = starboardConfig.getLvl().get(i);
            configManagementService.createIfNotExists(server.getId(), "starLvl" + ( i + 1 ), Double.valueOf(value));
        }
    }
}
