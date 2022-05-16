package dev.sheldan.abstracto.core.command.condition;


import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.concurrent.CompletableFuture;

public interface CommandCondition {
    default ConditionResult shouldExecute(CommandContext commandContext, Command command) {
        return ConditionResult.fromSuccess();
    }

    default ConditionResult shouldExecute(SlashCommandInteractionEvent slashCommandInteractionEvent, Command command) {
        return ConditionResult.fromSuccess();
    }

    default boolean isAsync() {
        return false;
    }
    default boolean supportsSlashCommands() {
        return false;
    }
    default CompletableFuture<ConditionResult> shouldExecuteAsync(CommandContext commandContext, Command command) {
        return CompletableFuture.completedFuture(ConditionResult.fromSuccess());
    }

    default CompletableFuture<ConditionResult> shouldExecuteAsync(SlashCommandInteractionEvent slashCommandInteractionEvent, Command command) {
        return CompletableFuture.completedFuture(ConditionResult.fromSuccess());
    }
}
