package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interactive.SetupExecution;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SetupService {
    CompletableFuture<Void> performSetup(FeatureConfig featureConfig, AServerChannelUserId user, Long initialMessageId);
    CompletableFuture<Void> executeSetup(FeatureConfig featureConfig, List<SetupExecution> steps, AServerChannelUserId user, List<DelayedActionConfig> delayedActionConfigs);
}
