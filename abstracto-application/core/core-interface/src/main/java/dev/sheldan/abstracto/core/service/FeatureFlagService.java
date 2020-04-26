package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureDisplay;
import dev.sheldan.abstracto.core.config.FeatureEnum;

import java.util.List;

public interface FeatureFlagService {
    boolean isFeatureEnabled(FeatureEnum name, Long serverId);
    void enableFeature(FeatureEnum name, Long serverId);
    void disableFeature(FeatureEnum name, Long serverId);
    List<String> getAllFeatures();
    List<FeatureDisplay> getAllFeatureDisplays();
    FeatureDisplay getFeatureDisplayforFeature(FeatureEnum featureEnum);
    boolean doesFeatureExist(FeatureEnum name);
    List<String> getFeaturesAsList();
    FeatureEnum getFeatureEnum(String key);
}
