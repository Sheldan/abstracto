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
public class DeleteChannelGroup implements Command {

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
        Parameter channelGroupName = Parameter.builder().name("name").type(String.class).description("The name of the channel group to delete.").build();
        List<Parameter> parameters = Arrays.asList(channelGroupName);
        List<String> aliases = Arrays.asList("-ChGroup");
        return CommandConfiguration.builder()
                .name("deleteChannelGroup")
                .module(ChannelsModuleInterface.CHANNELS)
                .parameters(parameters)
                .aliases(aliases)
                .description("Removes an existing channel group.")
                .causesReaction(true)
                .build();
    }

    @Override
    public String getFeature() {
        return AbstractoFeatures.CORE;
    }
}
