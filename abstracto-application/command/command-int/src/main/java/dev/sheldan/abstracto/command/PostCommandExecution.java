package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;

public interface PostCommandExecution {
    void execute(CommandContext commandContext, Result result, Command command);
}
