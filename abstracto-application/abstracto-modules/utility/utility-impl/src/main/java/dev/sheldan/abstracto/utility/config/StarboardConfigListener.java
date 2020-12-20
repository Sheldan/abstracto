package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.listener.sync.entity.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.utility.service.StarboardServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StarboardConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Creating starboard config for server {}", server.getId());
        int maxLevels = defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY).getLongValue().intValue();
        for (int i = 0; i < maxLevels; i++) {
            String configKey = StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + (i + 1);
            Integer value = defaultConfigManagementService.getDefaultConfig(configKey).getLongValue().intValue();
            configManagementService.createIfNotExists(server.getId(), configKey, Long.valueOf(value));
        }
    }
}
