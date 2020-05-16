package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureConfigLoader;
import dev.sheldan.abstracto.core.models.config.FeaturePropertiesConfig;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeatureFlagListener implements ServerConfigListener {

    @Autowired
    private FeatureConfigService featureFlagService;

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
    private FeatureConfigLoader featureConfigLoader;

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;


    @Override
    public void updateServerConfig(AServer server) {
        log.info("Setting up feature flags if necessary.");
        featureFlagService.getAllFeatureConfigs().forEach((featureFlagKey) -> {
            String featureKey = featureFlagKey.getFeature().getKey();
            AFeature feature = featureManagementService.getFeature(featureKey);
            FeaturePropertiesConfig featurePropertiesConfig = featureConfigLoader.getFeatures().get(featureKey);
            if(service.getFeatureFlag(feature, server.getId()) == null) {
                service.createFeatureFlag(feature, server.getId(), featurePropertiesConfig.getEnabled());
            }
            if(featurePropertiesConfig.getDefaultMode() != null && !featureModeManagementService.featureModeSet(feature, server)) {
                featureModeService.createMode(feature, server, featurePropertiesConfig.getDefaultMode());
            }
        });
    }
}
