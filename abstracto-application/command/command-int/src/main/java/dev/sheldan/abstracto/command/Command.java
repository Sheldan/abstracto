package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;

public interface Command<T> {

    Result execute(CommandContext commandContext);
    CommandConfiguration getConfiguration();
}
