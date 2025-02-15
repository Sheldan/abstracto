package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.exception.SlashCommandParameterMissingException;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RemoveFromChannelGroup extends AbstractConditionableCommand {

    private static final String REMOVE_FROM_CHANNEL_GROUP_COMMAND = "removeFromChannelGroup";
    private static final String CHANNEL_PARAMETER = "channel";
    private static final String NAME_PARAMETER = "name";
    private static final String REMOVE_FROM_CHANNEL_GROUP_RESPONSE = "removeFromChannelGroup_response";
    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String channelGroupName = slashCommandParameterService.getCommandOption(NAME_PARAMETER, event, String.class);
        AChannel actualChannel;
        if (slashCommandParameterService.hasCommandOptionWithFullType(CHANNEL_PARAMETER, event, OptionType.CHANNEL)) {
            GuildMessageChannel guildChannel =
                slashCommandParameterService.getCommandOption(CHANNEL_PARAMETER, event, AChannel.class, GuildMessageChannel.class);
            actualChannel = channelManagementService.loadChannel(guildChannel.getIdLong());
        } else if (slashCommandParameterService.hasCommandOptionWithFullType(CHANNEL_PARAMETER, event, OptionType.STRING)) {
            String channelId = slashCommandParameterService.getCommandOption(CHANNEL_PARAMETER, event, AChannel.class, String.class);
            actualChannel = channelManagementService.loadChannel(Long.parseLong(channelId));
        } else {
            throw new SlashCommandParameterMissingException(CHANNEL_PARAMETER);
        }
        channelGroupService.removeChannelFromChannelGroup(channelGroupName, actualChannel);
        return interactionService.replyEmbed(REMOVE_FROM_CHANNEL_GROUP_RESPONSE, event)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter
            .builder()
            .name(NAME_PARAMETER)
            .type(String.class)
            .build();
        Parameter channelToAdd = Parameter
            .builder()
            .name(CHANNEL_PARAMETER)
            .type(AChannel.class)
            .build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelToAdd);
        List<String> aliases = Arrays.asList("rmChChgrp", "chGrpCh-");
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .hasExample(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(CoreSlashCommandNames.CHANNELS)
            .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
            .commandName(REMOVE_FROM_CHANNEL_GROUP_COMMAND)
            .build();

        return CommandConfiguration.builder()
            .name(REMOVE_FROM_CHANNEL_GROUP_COMMAND)
            .module(ChannelsModuleDefinition.CHANNELS)
            .aliases(aliases)
            .slashCommandOnly(true)
            .parameters(parameters)
            .slashCommandConfig(slashCommandConfig)
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
