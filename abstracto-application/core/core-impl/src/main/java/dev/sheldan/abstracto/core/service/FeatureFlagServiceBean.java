package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlagServiceBean implements FeatureFlagService {

    @Autowired
    private FeatureFlagManagementService managementService;

    @Override
    public boolean isFeatureEnabled(String name, Long serverId) {
        return managementService.getFeatureFlagValue(name, serverId);
    }
}
