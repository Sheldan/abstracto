package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.ChannelGroupTypeManagementService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CreateChannelGroup extends AbstractConditionableCommand {

    private static final String CREATE_CHANNEL_GROUP_COMMAND = "createChannelGroup";
    private static final String NAME_PARAMETER = "name";
    private static final String GROUP_TYPE_PARAMETER = "groupType";
    private static final String CREATE_CHANNEL_GROUP_RESPONSE_TEMPLATE = "createChannelGroup_response";

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private ChannelGroupTypeManagementService channelGroupTypeManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String channelGroupName = slashCommandParameterService.getCommandOption(NAME_PARAMETER, event, String.class);
        String channelGroupType = slashCommandParameterService.getCommandOption(GROUP_TYPE_PARAMETER, event, ChannelGroupType.class, String.class);

        ChannelGroupType actualGroupType = channelGroupTypeManagementService.findChannelGroupTypeByKey((channelGroupType).trim());
        channelGroupService.createChannelGroup(channelGroupName, event.getGuild().getIdLong(), actualGroupType);
        return interactionService.replyEmbed(CREATE_CHANNEL_GROUP_RESPONSE_TEMPLATE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter
                .builder()
                .name(NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter channelGroupType = Parameter
                .builder()
                .name(GROUP_TYPE_PARAMETER)
                .type(ChannelGroupType.class)
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .rootCommandName(CoreSlashCommandNames.CHANNELS)
                .commandName(CREATE_CHANNEL_GROUP_COMMAND)
                .build();

        List<Parameter> parameters = Arrays.asList(channelGroupName, channelGroupType);
        List<String> aliases = Arrays.asList("+ChGroup");
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(CREATE_CHANNEL_GROUP_COMMAND)
                .module(ChannelsModuleDefinition.CHANNELS)
                .parameters(parameters)
                .aliases(aliases)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
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
}
