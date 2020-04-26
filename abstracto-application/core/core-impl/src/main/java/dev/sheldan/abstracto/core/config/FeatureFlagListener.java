package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeatureFlagListener implements ServerConfigListener {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureFlagManagementService service;

    @Autowired
    private Environment environment;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Setting up feature flags if necessary.");
        featureFlagService.getAllFeatureDisplays().forEach((featureFlagKey) -> {
            boolean featureFlagValue = BooleanUtils.toBoolean(environment.getProperty("abstracto.features." + featureFlagKey.getFeature().getKey(), "false"));
            if(!service.getFeatureFlag(featureFlagKey.getFeature(), server.getId()).isPresent()) {
                service.createFeatureFlag(featureFlagKey.getFeature(), server.getId(), featureFlagValue);
            }
        });
    }
}
