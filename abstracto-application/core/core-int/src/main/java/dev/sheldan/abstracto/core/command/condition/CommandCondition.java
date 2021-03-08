package dev.sheldan.abstracto.core.command.condition;


import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;

public interface CommandCondition {
    ConditionResult shouldExecute(CommandContext commandContext, Command command);
}
