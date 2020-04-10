package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.Optional;

public interface FeatureFlagManagementService {
    void createFeatureFlag(String key, Long serverId, Boolean newValue);
    void createFeatureFlag(String key, AServer server, Boolean newValue);
    boolean getFeatureFlagValue(String key, Long serverId);
    void updateFeatureFlag(String key, Long serverId, Boolean newValue);
    Optional<AFeatureFlag> getFeatureFlag(String key, Long serverId);
}
