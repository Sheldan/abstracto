package dev.sheldan.abstracto.command.service;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.CommandResult;

public interface PostCommandExecution {
    void execute(CommandContext commandContext, CommandResult commandResult, Command command);
}
