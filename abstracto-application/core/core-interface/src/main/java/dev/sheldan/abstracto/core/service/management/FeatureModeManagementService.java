package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface FeatureModeManagementService {
    AFeatureMode createMode(AFeatureFlag featureFlag, FeatureMode mode);
    AFeatureMode getModeForFeature(AFeatureFlag featureFlag);
    boolean featureModeSet(AFeature aFeature, AServer server);
    AFeatureMode setModeForFeature(AFeatureFlag featureFlag, FeatureMode featureMode);
}
