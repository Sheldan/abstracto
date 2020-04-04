package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.CommandContext;

public interface CommandCondition {
    boolean shouldExecute(CommandContext commandContext, Command command);
}
