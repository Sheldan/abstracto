package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeatureFlagListener implements ServerConfigListener {

    @Autowired
    private FeatureFlagConfig featureFlagConfig;

    @Autowired
    private FeatureFlagManagementServiceBean service;

    @Override
    public void updateServerConfig(ServerDto server) {
        log.info("Setting up feature flags if necessary.");
        featureFlagConfig.getFeatures().forEach((featureFlagKey, featureFlagValue) -> {
            if(!service.getFeatureFlag(featureFlagKey, server.getId()).isPresent()) {
                service.createFeatureFlag(featureFlagKey, server.getId(), featureFlagValue);
            }
        });
    }
}
