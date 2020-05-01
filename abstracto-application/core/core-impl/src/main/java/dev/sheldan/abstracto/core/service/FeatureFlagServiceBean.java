package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FeatureFlagServiceBean implements FeatureFlagService {

    @Autowired
    private FeatureFlagManagementService managementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private List<FeatureConfig> availableFeatures;

    @Autowired
    private ServerManagementService serverManagementService;


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
        FeatureEnum feature = name.getFeature();
        if(!doesFeatureExist(name)) {
            throw new FeatureNotFoundException("Feature not found.", feature.getKey(), getFeaturesAsList());
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
        FeatureEnum feature = name.getFeature();
        if(!doesFeatureExist(name)) {
            throw new FeatureNotFoundException("Feature not found.", feature.getKey(), getFeaturesAsList());
        }
        updateFeatureFlag(feature, server, false);
    }

    @Override
    public List<String> getAllFeatures() {
        return availableFeatures
                .stream()
                .map(featureDisplay -> featureDisplay.getFeature().getKey())
                .collect(Collectors.toList());
    }

    @Override
    public List<FeatureConfig> getAllFeatureConfigs() {
        return availableFeatures;
    }

    @Override
    public FeatureConfig getFeatureDisplayForFeature(FeatureEnum featureEnum) {
        Optional<FeatureConfig> any = getAllFeatureConfigs().stream().filter(featureDisplay -> featureDisplay.getFeature().equals(featureEnum)).findAny();
        if(any.isPresent()) {
            return any.get();
        }
        throw new AbstractoRunTimeException(String.format("Feature %s not found in configuration", featureEnum.getKey()));
    }

    @Override
    public FeatureConfig getFeatureDisplayForFeature(String key) {
        return getFeatureDisplayForFeature(getFeatureEnum(key));
    }

    @Override
    public boolean doesFeatureExist(FeatureConfig name) {
        return availableFeatures.stream().anyMatch(featureDisplay -> featureDisplay.getFeature().equals(name.getFeature()));
    }

    @Override
    public List<String> getFeaturesAsList() {
        return availableFeatures
                .stream()
                .map(featureDisplay -> featureDisplay.getFeature().getKey())
                .collect(Collectors.toList());
    }

    @Override
    public FeatureEnum getFeatureEnum(String key) {
        Optional<FeatureConfig> foundFeature = availableFeatures.stream().filter(featureDisplay -> featureDisplay.getFeature().getKey().equals(key)).findAny();
        if(foundFeature.isPresent()) {
            return foundFeature.get().getFeature();
        }
        throw new AbstractoRunTimeException(String.format("Feature %s not found.", key));
    }
    @Override
    public boolean getFeatureFlagValue(FeatureEnum key, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return getFeatureFlagValue(key, server);
    }

    @Override
    public boolean getFeatureFlagValue(FeatureEnum key, AServer server) {
        AFeature feature = featureManagementService.getFeature(key.getKey());
        AFeatureFlag featureFlag = managementService.getFeatureFlag(feature, server);
        return featureFlag.isEnabled();
    }

    @Override
    public AFeatureFlag updateFeatureFlag(FeatureEnum key, Long serverId, Boolean newValue) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return updateFeatureFlag(key, server, newValue);
    }

    @Override
    public AFeatureFlag updateFeatureFlag(FeatureEnum key, AServer server, Boolean newValue) {
        AFeature feature = featureManagementService.getFeature(key.getKey());
        return managementService.setFeatureFlagValue(feature, server, newValue);
    }
}
