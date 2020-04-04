package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.CommandResult;

public interface PostCommandExecution {
    void execute(CommandContext commandContext, CommandResult commandResult, Command command);
}
