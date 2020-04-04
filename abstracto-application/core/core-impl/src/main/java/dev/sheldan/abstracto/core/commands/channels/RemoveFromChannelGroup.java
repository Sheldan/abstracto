package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.Parameter;
import dev.sheldan.abstracto.core.config.AbstractoFeatures;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RemoveFromChannelGroup implements Command {


    @Autowired
    private ChannelGroupService channelGroupService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        TextChannel channel = (TextChannel) commandContext.getParameters().getParameters().get(1);
        channelGroupService.removeChannelFromChannelGroup(name, channel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("name").type(String.class).description("The name of the channel group to remove the channel from.").build();
        Parameter channelToAdd = Parameter.builder().name("channel").type(TextChannel.class).description("The mention of the channel to remove from the group.").build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelToAdd);
        List<String> aliases = Arrays.asList("rmChChgrp", "chGrpCh-");
        return CommandConfiguration.builder()
                .name("removeFromChannelGroup")
                .module("channels")
                .aliases(aliases)
                .parameters(parameters)
                .description("Removes the mentioned channel from the channel group.")
                .causesReaction(true)
                .build();
    }

    @Override
    public String getFeature() {
        return AbstractoFeatures.CORE;
    }
}
