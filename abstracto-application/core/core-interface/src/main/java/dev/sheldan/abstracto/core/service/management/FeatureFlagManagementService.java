package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagManagementService {
    void createFeatureFlag(AFeature feature, Long serverId, Boolean newValue);
    void createFeatureFlag(AFeature feature, AServer server, Boolean newValue);
    boolean getFeatureFlagValue(FeatureEnum key, Long serverId);
    boolean getFeatureFlagValue(FeatureEnum key, AServer server);
    void updateFeatureFlag(FeatureEnum key, Long serverId, Boolean newValue);
    void updateFeatureFlag(FeatureEnum key, AServer server, Boolean newValue);
    Optional<AFeatureFlag> getFeatureFlag(AFeature key, Long serverId);
    Optional<AFeatureFlag> getFeatureFlag(AFeature key, AServer server);
    List<AFeatureFlag> getFeatureFlagsOfServer(AServer server);
}
