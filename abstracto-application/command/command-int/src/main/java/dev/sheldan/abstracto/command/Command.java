package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Result;

public interface Command<T> {

    Result execute(Context context);
    Configuration getConfiguration();
}
