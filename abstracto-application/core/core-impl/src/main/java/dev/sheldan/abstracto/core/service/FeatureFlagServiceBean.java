package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureDisplay;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
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
    private List<FeatureDisplay> availableFeatures;


    @Override
    public boolean isFeatureEnabled(FeatureEnum name, Long serverId) {
        return managementService.getFeatureFlagValue(name, serverId);
    }

    @Override
    public void enableFeature(FeatureEnum name, Long serverId) {
        if(!doesFeatureExist(name)) {
            throw new FeatureNotFoundException("Feature not found.", name.getKey(), getFeaturesAsList());
        }
        managementService.updateFeatureFlag(name, serverId, true);
    }

    @Override
    public void disableFeature(FeatureEnum name, Long serverId) {
        if(!doesFeatureExist(name)) {
            throw new FeatureNotFoundException("Feature not found.", name.getKey(), getFeaturesAsList());
        }
        managementService.updateFeatureFlag(name, serverId, false);
    }

    @Override
    public List<String> getAllFeatures() {
        return availableFeatures
                .stream()
                .map(featureDisplay -> featureDisplay.getFeature().getKey())
                .collect(Collectors.toList());
    }

    @Override
    public List<FeatureDisplay> getAllFeatureDisplays() {
        return availableFeatures;
    }

    @Override
    public FeatureDisplay getFeatureDisplayforFeature(FeatureEnum featureEnum) {
        Optional<FeatureDisplay> any = getAllFeatureDisplays().stream().filter(featureDisplay -> featureDisplay.getFeature().equals(featureEnum)).findAny();
        if(any.isPresent()) {
            return any.get();
        }
        throw new AbstractoRunTimeException(String.format("Feature %s not found in configuration", featureEnum.getKey()));
    }

    @Override
    public boolean doesFeatureExist(FeatureEnum name) {
        return availableFeatures.stream().anyMatch(featureDisplay -> featureDisplay.getFeature().equals(name));
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
        Optional<FeatureDisplay> foundFeature = availableFeatures.stream().filter(featureDisplay -> featureDisplay.getFeature().getKey().equals(key)).findAny();
        if(foundFeature.isPresent()) {
            return foundFeature.get().getFeature();
        }
        throw new AbstractoRunTimeException(String.format("Feature %s not found.", key));
    }
}
