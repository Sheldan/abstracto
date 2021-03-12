package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FeatureFlagServiceBean implements FeatureFlagService {

    @Autowired
    private FeatureFlagManagementService managementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private DefaultFeatureFlagManagementService defaultFeatureFlagManagementService;

    @Override
    public boolean isFeatureEnabled(FeatureConfig name, Long serverId) {
        return getFeatureFlagValue(name.getFeature(), serverId);
    }

    @Override
    public boolean isFeatureEnabled(FeatureConfig name, AServer server) {
        return getFeatureFlagValue(name.getFeature(), server);
    }

    @Override
    public void enableFeature(FeatureConfig name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        enableFeature(name, server);
    }

    @Override
    public void enableFeature(FeatureConfig name, AServer server) {
        FeatureDefinition feature = name.getFeature();
        if(!featureConfigService.doesFeatureExist(name)) {
            throw new FeatureNotFoundException(feature.getKey(), featureConfigService.getFeaturesAsList());
        }
        updateFeatureFlag(feature, server, true);
    }

    @Override
    public void disableFeature(FeatureConfig name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        disableFeature(name, server);
    }

    @Override
    public void disableFeature(FeatureConfig name, AServer server) {
        FeatureDefinition feature = name.getFeature();
        if(!featureConfigService.doesFeatureExist(name)) {
            throw new FeatureNotFoundException(feature.getKey(), featureConfigService.getFeaturesAsList());
        }
        updateFeatureFlag(feature, server, false);
    }

    @Override
    public AFeatureFlag createInstanceFromDefaultConfig(FeatureDefinition name, Long serverId) {
        FeatureFlagProperty defaultFeatureFlag = defaultFeatureFlagManagementService.getDefaultFeatureFlagProperty(name);
        return updateFeatureFlag(name, serverId, defaultFeatureFlag.getEnabled());
    }

    @Override
    public AFeatureFlag createInstanceFromDefaultConfig(FeatureDefinition name, AServer server) {
        FeatureFlagProperty defaultFeatureFlag = defaultFeatureFlagManagementService.getDefaultFeatureFlagProperty(name);
        return updateFeatureFlag(name, server, defaultFeatureFlag.getEnabled());
    }

    @Override
    public boolean getFeatureFlagValue(FeatureDefinition key, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return getFeatureFlagValue(key, server);
    }

    @Override
    public boolean getFeatureFlagValue(FeatureDefinition key, AServer server) {
        AFeature feature = featureManagementService.getFeature(key.getKey());
        Optional<AFeatureFlag> featureFlagOptional = managementService.getFeatureFlag(feature, server);
        return featureFlagOptional
                .map(AFeatureFlag::isEnabled)
                .orElseGet(() -> defaultFeatureFlagManagementService.getDefaultFeatureFlagProperty(feature).getEnabled());
    }

    @Override
    public AFeatureFlag updateFeatureFlag(FeatureDefinition key, Long serverId, Boolean newValue) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return updateFeatureFlag(key, server, newValue);
    }

    @Override
    public AFeatureFlag updateFeatureFlag(FeatureDefinition key, AServer server, Boolean newValue) {
        AFeature feature = featureManagementService.getFeature(key.getKey());
        return managementService.setFeatureFlagValue(feature, server, newValue);
    }
}
