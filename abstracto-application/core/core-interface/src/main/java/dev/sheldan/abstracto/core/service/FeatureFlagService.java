package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
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
    PostTargetEnum getPostTargetEnumByKey(String key);
    boolean getFeatureFlagValue(FeatureEnum key, Long serverId);
    boolean getFeatureFlagValue(FeatureEnum key, AServer server);
    AFeatureFlag updateFeatureFlag(FeatureEnum key, Long serverId, Boolean newValue);
    AFeatureFlag updateFeatureFlag(FeatureEnum key, AServer server, Boolean newValue);
    FeatureValidationResult validateFeatureSetup(FeatureConfig featureConfig, AServer server);
}
