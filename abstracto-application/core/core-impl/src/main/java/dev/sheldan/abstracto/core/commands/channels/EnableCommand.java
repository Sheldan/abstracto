package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.config.AbstractoFeatures;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EnableCommand implements Command {

    @Autowired
    private ChannelGroupService channelGroupService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String commandName = (String) commandContext.getParameters().getParameters().get(0);
        String channelGroupName = (String) commandContext.getParameters().getParameters().get(1);
        channelGroupService.enableCommandInChannelGroup(commandName, channelGroupName, commandContext.getGuild().getIdLong());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("commandName").type(String.class).description("The name of the channel group to add the channel to.").build();
        Parameter channelToAdd = Parameter.builder().name("channelGroup").type(String.class).description("The name of the channel group in which the command should be disabled.").build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelToAdd);
        return CommandConfiguration.builder()
                .name("enableCommand")
                .module(ChannelsModuleInterface.CHANNELS)
                .parameters(parameters)
                .description("Disables the given command in the given channel group.")
                .causesReaction(true)
                .build();
    }

    @Override
    public String getFeature() {
        return AbstractoFeatures.CORE;
    }
}
