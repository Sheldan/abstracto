package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.moderation.config.features.WarningDecayFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WarnDecayConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        configManagementService.createIfNotExists(server.getId(), WarningDecayFeature.DECAY_DAYS_KEY, defaultConfigManagementService.getDefaultConfig(WarningDecayFeature.DECAY_DAYS_KEY).getLongValue());
    }
}
