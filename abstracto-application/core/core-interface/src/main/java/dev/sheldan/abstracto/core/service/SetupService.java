package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interactive.SetupExecution;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;

import java.util.List;

public interface SetupService {
    void performSetup(FeatureConfig featureConfig, AServerChannelUserId user, Long initialMessageId);
    void executeSetup(FeatureConfig featureConfig, List<SetupExecution> steps, AServerChannelUserId user, List<DelayedActionConfig> delayedActionConfigs);
}
