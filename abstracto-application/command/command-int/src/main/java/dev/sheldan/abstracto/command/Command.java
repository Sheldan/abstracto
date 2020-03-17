package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;

public interface Command<T> {

    Result execute(CommandContext commandContext);
    Configuration getConfiguration();
}
