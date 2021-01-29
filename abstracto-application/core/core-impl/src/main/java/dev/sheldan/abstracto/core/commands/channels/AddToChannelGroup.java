package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AddToChannelGroup extends AbstractConditionableCommand {


    @Autowired
    private ChannelGroupService channelGroupService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        TextChannel channel = (TextChannel) commandContext.getParameters().getParameters().get(1);
        channelGroupService.addChannelToChannelGroup(name, channel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("name").type(String.class).templated(true).build();
        Parameter channelToAdd = Parameter.builder().name("channel").type(TextChannel.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelToAdd);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        List<String> aliases = Arrays.asList("addTChGrp", "chGrpCh+");
        return CommandConfiguration.builder()
                .name("addToChannelGroup")
                .module(ChannelsModuleInterface.CHANNELS)
                .aliases(aliases)
                .parameters(parameters)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
