package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface FeatureModeService {
    AFeatureMode setModeForFeatureTo(String key, AServer server, String newMode);
    AFeatureMode setModeForFeatureTo(AFeatureFlag flag, String newMode);
    AFeatureMode setModeForFeatureTo(FeatureEnum featureEnum, AServer server, String newMode);
    AFeatureMode setModeForFeatureTo(AFeature featureEnum, AServer server, String newMode);
    AFeatureMode setModeForFeatureTo(FeatureEnum featureEnum, AServer server, FeatureMode mode);
    AFeatureMode setModeForFeatureTo(AFeature feature, AServer server, FeatureMode mode);
    AFeatureMode setModeForFeatureTo(AFeatureFlag featureFlag, FeatureMode mode);

    AFeatureMode createMode(String key, AServer server, String newMode);
    AFeatureMode createMode(AFeatureFlag flag, String newMode);
    AFeatureMode createMode(FeatureEnum featureEnum, AServer server, String newMode);
    AFeatureMode createMode(AFeature featureEnum, AServer server, String newMode);
    AFeatureMode createMode(FeatureEnum featureEnum, AServer server, FeatureMode mode);
    AFeatureMode createMode(AFeature feature, AServer server, FeatureMode mode);
    AFeatureMode createMode(AFeatureFlag featureFlag, FeatureMode mode);

    AFeatureMode getFeatureMode(FeatureEnum featureEnum, AServer server);
    AFeatureMode getFeatureMode(AFeature feature, AServer server);
}
