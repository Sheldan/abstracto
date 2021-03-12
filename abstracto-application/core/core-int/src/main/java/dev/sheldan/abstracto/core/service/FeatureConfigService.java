package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;

public interface FeatureConfigService {
    List<String> getAllFeatures();
    List<FeatureConfig> getAllFeatureConfigs();
    FeatureConfig getFeatureDisplayForFeature(FeatureDefinition featureDefinition);
    FeatureConfig getFeatureDisplayForFeature(String key);
    boolean doesFeatureExist(FeatureConfig name);
    boolean doesFeatureExist(String name);
    List<String> getFeaturesAsList();
    List<String> getFeatureModesFromFeatureAsString(String featureName);
    FeatureDefinition getFeatureEnum(String key);
    PostTargetEnum getPostTargetEnumByKey(String key);
    FeatureValidationResult validateFeatureSetup(FeatureConfig featureConfig, AServer server);
    FeatureMode getFeatureModeByKey(FeatureConfig featureConfig, String key);
    FeatureConfig getFeatureConfigForFeature(AFeature feature);
    boolean isModeValid(String featureName, String modeName);
}
