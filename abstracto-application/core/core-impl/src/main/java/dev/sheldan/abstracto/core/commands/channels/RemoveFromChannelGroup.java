package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RemoveFromChannelGroup extends AbstractConditionableCommand {

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        AChannel fakeChannel = (AChannel) commandContext.getParameters().getParameters().get(1);
        AChannel actualChannel = channelManagementService.loadChannel(fakeChannel.getId());
        if(!actualChannel.getServer().getId().equals(commandContext.getGuild().getIdLong())) {
            throw new EntityGuildMismatchException();
        }
        channelGroupService.removeChannelFromChannelGroup(name, actualChannel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("name").type(String.class).build();
        Parameter channelToAdd = Parameter.builder().name("channel").type(AChannel.class).build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelToAdd);
        List<String> aliases = Arrays.asList("rmChChgrp", "chGrpCh-");
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("removeFromChannelGroup")
                .module(ChannelsModuleDefinition.CHANNELS)
                .aliases(aliases)
                .parameters(parameters)
                .templated(true)
                .help(helpInfo)
                .supportsEmbedException(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
