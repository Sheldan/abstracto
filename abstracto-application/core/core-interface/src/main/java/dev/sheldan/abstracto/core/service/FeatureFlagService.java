package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;

public interface FeatureFlagService {
    boolean isFeatureEnabled(FeatureConfig name, Long serverId);
    boolean isFeatureEnabled(FeatureConfig name, AServer server);
    void enableFeature(FeatureConfig name, Long serverId);
    void enableFeature(FeatureConfig name, AServer server);
    void disableFeature(FeatureConfig name, Long serverId);
    void disableFeature(FeatureConfig name, AServer server);
    List<String> getAllFeatures();
    List<FeatureConfig> getAllFeatureConfigs();
    FeatureConfig getFeatureDisplayForFeature(FeatureEnum featureEnum);
    FeatureConfig getFeatureDisplayForFeature(String key);
    boolean doesFeatureExist(FeatureConfig name);
    List<String> getFeaturesAsList();
    FeatureEnum getFeatureEnum(String key);
}
