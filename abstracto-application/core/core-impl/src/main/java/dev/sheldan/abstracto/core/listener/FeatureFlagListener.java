package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FeatureFlagListener implements ServerConfigListener {

    @Autowired
    private FeatureFlagManagementService service;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private FeatureModeManagementService featureModeManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private Environment environment;

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Autowired
    private DefaultFeatureFlagManagementService defaultFeatureFlagManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Setting up feature flags if necessary.");
        List<String> defaultFeatureKeys = defaultFeatureFlagManagementService.getDefaultFeatureKeys();
        defaultFeatureFlagManagementService.getAllDefaultFeatureFlags().forEach(featureFlagKey -> {
            String featureKey = featureFlagKey.getFeature().getKey();
            AFeature feature = featureManagementService.getFeature(featureKey);
            if(defaultFeatureKeys.contains(featureKey)) {
                if(service.getFeatureFlag(feature, server.getId()) == null) {
                    log.info("Creating feature flag {} for server {}.", feature.getKey(), server.getId());
                    service.createFeatureFlag(feature, server.getId(), featureFlagKey.isEnabled());
                } else {
                    log.trace("Feature flag {} for server {} already exists.", feature.getKey(), server.getId());
                }
                if(featureFlagKey.getMode() != null && !featureModeManagementService.featureModeSet(feature, server)) {
                    featureModeService.createMode(feature, server, featureFlagKey.getMode());
                }
            } else {
                log.warn("Feature {} was found as interface, but not in the properties configuration. It will not be setup.", featureKey);
            }
        });
    }
}
