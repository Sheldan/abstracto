package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.CommandResult;
import dev.sheldan.abstracto.core.listener.FeatureAware;

public interface Command extends FeatureAware {

    CommandResult execute(CommandContext commandContext);
    CommandConfiguration getConfiguration();
}
