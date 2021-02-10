package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagManagementService {
    AFeatureFlag createFeatureFlag(AFeature feature, Long serverId, Boolean newValue);
    AFeatureFlag createFeatureFlag(AFeature feature, AServer server, Boolean newValue);
    Optional<AFeatureFlag> getFeatureFlag(AFeature feature, Long serverId);
    Optional<AFeatureFlag> getFeatureFlag(String featureKey, Long serverId);
    Optional<AFeatureFlag> getFeatureFlag(String featureKey, AServer server);
    boolean featureFlagExists(AFeature feature, AServer server);
    Optional<AFeatureFlag> getFeatureFlag(AFeature feature, AServer server);
    List<AFeatureFlag> getFeatureFlagsOfServer(AServer server);
    AFeatureFlag setFeatureFlagValue(AFeature feature, Long serverId, Boolean newValue);
    AFeatureFlag setFeatureFlagValue(AFeature feature, AServer server, Boolean newValue);
}
