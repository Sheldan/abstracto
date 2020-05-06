package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.modmail.commands.condition.RequiresModMailCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnonReply extends AbstractConditionableCommand {

    @Autowired
    private RequiresModMailCondition requiresModMailCondition;

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

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(requiresModMailCondition);
        return conditions;
    }
}
