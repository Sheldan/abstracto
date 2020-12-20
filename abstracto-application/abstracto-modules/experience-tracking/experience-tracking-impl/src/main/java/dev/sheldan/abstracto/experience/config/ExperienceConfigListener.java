package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.listener.sync.entity.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listener responsible to configure the required experience configurations in case the bot joins a new server.
 */
@Component
@Slf4j
public class ExperienceConfigListener implements ServerConfigListener {


    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private ConfigManagementService service;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Setting up experience configuration for server {}.", server.getId());
        service.createIfNotExists(server.getId(), ExperienceFeatureConfig.MIN_EXP_KEY, defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.MIN_EXP_KEY).getLongValue());
        service.createIfNotExists(server.getId(), ExperienceFeatureConfig.MAX_EXP_KEY, defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.MAX_EXP_KEY).getLongValue());
        service.createIfNotExists(server.getId(), ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, defaultConfigManagementService.getDefaultConfig(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY).getDoubleValue());
    }
}
