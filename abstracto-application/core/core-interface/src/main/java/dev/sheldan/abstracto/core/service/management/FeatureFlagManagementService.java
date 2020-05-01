package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;

public interface FeatureFlagManagementService {
    AFeatureFlag createFeatureFlag(AFeature feature, Long serverId, Boolean newValue);
    AFeatureFlag createFeatureFlag(AFeature feature, AServer server, Boolean newValue);
    AFeatureFlag getFeatureFlag(AFeature key, Long serverId);
    AFeatureFlag getFeatureFlag(AFeature key, AServer server);
    List<AFeatureFlag> getFeatureFlagsOfServer(AServer server);
    AFeatureFlag setFeatureFlagValue(AFeature feature, Long serverId, Boolean newValue);
    AFeatureFlag setFeatureFlagValue(AFeature feature, AServer server, Boolean newValue);
}
