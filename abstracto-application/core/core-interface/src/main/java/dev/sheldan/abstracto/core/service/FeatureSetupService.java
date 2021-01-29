package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.interactive.SetupExecution;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FeatureSetupService {
    CompletableFuture<Void> performFeatureSetup(FeatureConfig featureConfig, AServerChannelUserId user, Long initialMessageId);
    CompletableFuture<Void> executeFeatureSetup(FeatureConfig featureConfig, List<SetupExecution> steps, AServerChannelUserId user, List<DelayedActionConfig> delayedActionConfigs);
}
