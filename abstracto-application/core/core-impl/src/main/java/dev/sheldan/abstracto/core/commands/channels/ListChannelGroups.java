package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.ListChannelGroupsModel;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ListChannelGroups extends AbstractConditionableCommand {

    public static final String LIST_CHANNEL_GROUPS_COMMAND = "listChannelGroups";
    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private InteractionService interactionService;

    private MessageToSend getMessageToSend(Guild guild) {
        AServer server = serverManagementService.loadServer(guild);
        List<AChannelGroup> channelGroups = channelGroupManagementService.findAllInServer(server);
        ListChannelGroupsModel template = ListChannelGroupsModel
            .builder()
            .groups(channelGroupService.convertAChannelGroupToChannelGroupChannel(channelGroups))
            .build();
        return templateService.renderEmbedTemplate("listChannelGroups_response", template, guild.getIdLong());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        MessageToSend response = getMessageToSend(event.getGuild());
        return interactionService.replyMessageToSend(response, event)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<String> aliases = Arrays.asList("lsChGrp");
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(CoreSlashCommandNames.CHANNELS)
            .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
            .commandName(LIST_CHANNEL_GROUPS_COMMAND)
            .build();

        return CommandConfiguration.builder()
            .name(LIST_CHANNEL_GROUPS_COMMAND)
            .module(ChannelsModuleDefinition.CHANNELS)
            .slashCommandConfig(slashCommandConfig)
            .aliases(aliases)
            .slashCommandOnly(true)
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
