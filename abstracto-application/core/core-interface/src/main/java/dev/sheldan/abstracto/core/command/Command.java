package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.listener.FeatureAware;

import java.io.Serializable;

public interface Command extends FeatureAware {

    CommandResult execute(CommandContext commandContext);
    CommandConfiguration getConfiguration();
}
