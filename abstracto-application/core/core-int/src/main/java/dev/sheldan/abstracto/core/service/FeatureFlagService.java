package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface FeatureFlagService {
    boolean isFeatureEnabled(FeatureConfig name, Long serverId);
    boolean isFeatureEnabled(FeatureConfig name, AServer server);
    void enableFeature(FeatureConfig name, Long serverId);
    void enableFeature(FeatureConfig name, AServer server);
    void disableFeature(FeatureConfig name, Long serverId);
    void disableFeature(FeatureConfig name, AServer server);
    AFeatureFlag createInstanceFromDefaultConfig(FeatureDefinition name, Long serverId);
    AFeatureFlag createInstanceFromDefaultConfig(FeatureDefinition name, AServer server);
    boolean getFeatureFlagValue(FeatureDefinition key, Long serverId);
    boolean getFeatureFlagValue(FeatureDefinition key, AServer server);
    AFeatureFlag updateFeatureFlag(FeatureDefinition key, Long serverId, Boolean newValue);
    AFeatureFlag updateFeatureFlag(FeatureDefinition key, AServer server, Boolean newValue);
}
