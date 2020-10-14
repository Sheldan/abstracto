package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

public interface FeatureModeManagementService {
    AFeatureMode createMode(AFeatureFlag featureFlag, FeatureMode mode, boolean enabled);
    AFeatureMode createMode(AFeatureFlag featureFlag, String mode, boolean enabled);

    boolean isFeatureModeActive(AFeature aFeature, AServer server, FeatureMode mode);
    boolean doesFeatureModeExist(AFeature aFeature, AServer server, FeatureMode mode);
    boolean doesFeatureModeExist(AFeature aFeature, AServer server, String modeKey);
    Optional<AFeatureMode> getFeatureMode(AFeature aFeature, AServer server, FeatureMode mode);
    Optional<AFeatureMode> getFeatureMode(AFeature aFeature, AServer server, String modeKey);
    List<AFeatureMode> getFeatureModesOfServer(AServer server);
    List<AFeatureMode> getFeatureModesOfFeatureInServer(AServer server, AFeature aFeature);
}
