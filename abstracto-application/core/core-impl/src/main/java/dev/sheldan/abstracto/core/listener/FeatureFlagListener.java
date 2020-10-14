package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class FeatureFlagListener implements ServerConfigListener {

    @Autowired
    private FeatureFlagManagementService service;

    @Autowired
    private DefaultFeatureFlagManagementService defaultFeatureFlagManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Setting up feature flags if necessary.");
        defaultFeatureFlagManagementService.getAllDefaultFeatureFlags().forEach(defaultFeatureFlag -> {
            AFeature feature = defaultFeatureFlag.getFeature();
            if(!service.featureFlagExists(feature, server)) {
                log.info("Creating feature flag {} for server {}.", feature.getKey(), server.getId());
                service.createFeatureFlag(feature, server.getId(), defaultFeatureFlag.isEnabled());
            } else {
                log.trace("Feature flag {} for server {} already exists.", feature.getKey(), server.getId());
            }
        });
    }
}
