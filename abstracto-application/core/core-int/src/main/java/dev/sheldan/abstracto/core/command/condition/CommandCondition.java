package dev.sheldan.abstracto.core.command.condition;


import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;

import java.util.concurrent.CompletableFuture;

public interface CommandCondition {
    default ConditionResult shouldExecute(CommandContext commandContext, Command command) {
        return ConditionResult.fromSuccess();
    }
    default boolean isAsync() {
        return false;
    }
    default CompletableFuture<ConditionResult> shouldExecuteAsync(CommandContext commandContext, Command command) {
        return CompletableFuture.completedFuture(ConditionResult.fromSuccess());
    }
}
