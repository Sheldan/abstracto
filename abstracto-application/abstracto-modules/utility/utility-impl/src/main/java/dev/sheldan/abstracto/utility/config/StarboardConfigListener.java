package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.utility.service.StarboardServiceBean;
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
        int levels = starboardConfig.getLvl().size();
        for (int i = 0; i < levels; i++) {
            Integer value = starboardConfig.getLvl().get(i);
            configManagementService.createIfNotExists(server.getId(), StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + ( i + 1 ), Long.valueOf(value));
        }
    }
}
