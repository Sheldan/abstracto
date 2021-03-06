package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DeleteChannelGroup extends AbstractConditionableCommand {

    @Autowired
    private ChannelGroupService channelGroupService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String groupName = (String) commandContext.getParameters().getParameters().get(0);
        channelGroupService.deleteChannelGroup(groupName, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("name").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(channelGroupName);
        List<String> aliases = Arrays.asList("-ChGroup");
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("deleteChannelGroup")
                .module(ChannelsModuleDefinition.CHANNELS)
                .parameters(parameters)
                .aliases(aliases)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
