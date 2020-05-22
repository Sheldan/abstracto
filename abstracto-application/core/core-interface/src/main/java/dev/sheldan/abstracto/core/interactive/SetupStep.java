package dev.sheldan.abstracto.core.interactive;


import dev.sheldan.abstracto.core.models.AServerChannelUserId;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SetupStep {
    CompletableFuture<List<DelayedActionConfig>> execute(AServerChannelUserId aUserInAServer, SetupStepParameter parameter);
}
