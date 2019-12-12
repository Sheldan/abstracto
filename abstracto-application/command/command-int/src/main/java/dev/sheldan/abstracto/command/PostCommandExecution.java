package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Result;

public interface PostCommandExecution {
    void execute(Context context, Result result, Command command);
}
