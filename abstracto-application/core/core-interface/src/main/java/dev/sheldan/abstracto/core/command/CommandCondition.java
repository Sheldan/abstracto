package dev.sheldan.abstracto.core.command;


import dev.sheldan.abstracto.core.command.execution.CommandContext;

public interface CommandCondition {
    ConditionResult shouldExecute(CommandContext commandContext, Command command);
}
