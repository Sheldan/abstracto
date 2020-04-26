package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagManagementService {
    void createFeatureFlag(FeatureEnum key, Long serverId, Boolean newValue);
    void createFeatureFlag(FeatureEnum key, AServer server, Boolean newValue);
    boolean getFeatureFlagValue(FeatureEnum key, Long serverId);
    void updateFeatureFlag(FeatureEnum key, Long serverId, Boolean newValue);
    Optional<AFeatureFlag> getFeatureFlag(FeatureEnum key, Long serverId);
    List<AFeatureFlag> getFeatureFlagsOfServer(AServer server);
}
