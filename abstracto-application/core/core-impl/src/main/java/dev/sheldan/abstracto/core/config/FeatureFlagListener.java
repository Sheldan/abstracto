package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AFeature;
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
    private FeatureManagementService featureManagementService;

    @Autowired
    private Environment environment;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Setting up feature flags if necessary.");
        featureFlagService.getAllFeatureConfigs().forEach((featureFlagKey) -> {
            String featureKey = featureFlagKey.getFeature().getKey();
            AFeature feature = featureManagementService.getFeature(featureKey);
            boolean featureFlagValue = BooleanUtils.toBoolean(environment.getProperty("abstracto.features." + featureKey, "false"));
            if(!service.getFeatureFlag(feature, server.getId()).isPresent()) {
                service.createFeatureFlag(feature, server.getId(), featureFlagValue);
            }
        });
    }
}
