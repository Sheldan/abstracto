package dev.sheldan.abstracto.core.interactive;


import dev.sheldan.abstracto.core.models.AServerChannelUserId;

import java.util.concurrent.CompletableFuture;

public interface SetupStep {
    CompletableFuture<SetupStepResult> execute(AServerChannelUserId aUserInAServer, SetupStepParameter parameter);
}
