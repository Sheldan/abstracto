package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureFlagConfig;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlagServiceBean implements FeatureFlagService {

    @Autowired
    private FeatureFlagManagementService managementService;

    @Autowired
    private FeatureFlagConfig featureFlagConfig;


    @Override
    public boolean isFeatureEnabled(String name, Long serverId) {
        return managementService.getFeatureFlagValue(name, serverId);
    }

    @Override
    public void enableFeature(String name, Long serverId) {
        if(!featureFlagConfig.doesFeatureExist(name)) {
            throw new FeatureNotFoundException("Feature not found.", name, featureFlagConfig.getFeaturesAsList());
        }
        managementService.updateFeatureFlag(name, serverId, true);
    }

    @Override
    public void disableFeature(String name, Long serverId) {
        if(!featureFlagConfig.doesFeatureExist(name)) {
            throw new FeatureNotFoundException("Feature not found.", name, featureFlagConfig.getFeaturesAsList());
        }
        managementService.updateFeatureFlag(name, serverId, false);
    }
}
