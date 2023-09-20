package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandDisabledService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EnableCommand extends AbstractConditionableCommand {

    @Autowired
    private CommandDisabledService commandDisabledService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String commandName = (String) commandContext.getParameters().getParameters().get(0);
        String channelGroupName = (String) commandContext.getParameters().getParameters().get(1);
        commandDisabledService.enableCommandInChannelGroup(commandName, channelGroupName, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter commandName = Parameter
                .builder()
                .name("commandName")
                .type(String.class)
                .templated(true)
                .build();
        Parameter channelGroupName = Parameter
                .builder()
                .name("channelGroupName")
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(commandName, channelGroupName);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("enableCommand")
                .module(ChannelsModuleDefinition.CHANNELS)
                .parameters(parameters)
                .messageCommandOnly(true)
                .supportsEmbedException(true)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.remove(commandDisabledCondition);
        return conditions;
    }
}
