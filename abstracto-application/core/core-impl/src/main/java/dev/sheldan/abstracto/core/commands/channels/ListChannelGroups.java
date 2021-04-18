package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.ListChannelGroupsModel;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ListChannelGroups extends AbstractConditionableCommand {

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

    @Override
    public CommandResult execute(CommandContext commandContext) {
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        List<AChannelGroup> channelGroups = channelGroupManagementService.findAllInServer(server);
        ListChannelGroupsModel template = (ListChannelGroupsModel) ContextConverter.fromCommandContext(commandContext, ListChannelGroupsModel.class);
        template.setGroups(channelGroupService.convertAChannelGroupToChannelGroupChannel(channelGroups));
        MessageToSend response = templateService.renderEmbedTemplate("listChannelGroups_response", template, commandContext.getGuild().getIdLong());
        channelService.sendMessageToSendToChannel(response, commandContext.getChannel());
        return CommandResult.fromIgnored();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<String> aliases = Arrays.asList("lsChGrp");
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("listChannelGroups")
                .module(ChannelsModuleDefinition.CHANNELS)
                .aliases(aliases)
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
