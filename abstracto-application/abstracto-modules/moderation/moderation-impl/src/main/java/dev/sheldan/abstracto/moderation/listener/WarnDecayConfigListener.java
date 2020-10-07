package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.moderation.config.features.WarningDecayFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WarnDecayConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Updating decay day configuration for server {}.", server.getId());
        configManagementService.createIfNotExists(server.getId(), WarningDecayFeature.DECAY_DAYS_KEY, defaultConfigManagementService.getDefaultConfig(WarningDecayFeature.DECAY_DAYS_KEY).getLongValue());
    }
}
