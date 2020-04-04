package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;

public interface PostCommandExecution {
    void execute(CommandContext commandContext, CommandResult commandResult, Command command);
}
