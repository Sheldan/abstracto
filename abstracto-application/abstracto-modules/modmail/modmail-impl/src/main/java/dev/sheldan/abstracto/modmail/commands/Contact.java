package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import org.springframework.stereotype.Component;

@Component
public class Contact extends AbstractConditionableCommand {
    @Override
    public CommandResult execute(CommandContext commandContext) {
        return null;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        return null;
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MODMAIL;
    }

}
