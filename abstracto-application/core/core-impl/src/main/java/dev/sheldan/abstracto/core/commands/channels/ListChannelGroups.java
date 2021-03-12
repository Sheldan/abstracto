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
import dev.sheldan.abstracto.core.models.template.commands.ChannelGroupChannelModel;
import dev.sheldan.abstracto.core.models.template.commands.ChannelGroupModel;
import dev.sheldan.abstracto.core.models.template.commands.ListChannelGroupsModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Override
    public CommandResult execute(CommandContext commandContext) {
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        List<AChannelGroup> channelGroups = channelGroupManagementService.findAllInServer(server);
        ListChannelGroupsModel template = (ListChannelGroupsModel) ContextConverter.fromCommandContext(commandContext, ListChannelGroupsModel.class);
        template.setGroups(convertAChannelGroupToChannelGroupChannel(channelGroups));
        MessageToSend response = templateService.renderEmbedTemplate("listChannelGroups_response", template, commandContext.getGuild().getIdLong());
        channelService.sendMessageToSendToChannel(response, commandContext.getChannel());
        return CommandResult.fromIgnored();
    }

    private List<ChannelGroupModel> convertAChannelGroupToChannelGroupChannel(List<AChannelGroup> channelGroups) {
        List<ChannelGroupModel> converted = new ArrayList<>();
        channelGroups.forEach(group -> {
            List<ChannelGroupChannelModel> convertedChannels = new ArrayList<>();
            group.getChannels().forEach(channel -> {
                Optional<TextChannel> textChannelInGuild = channelService.getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
                ChannelGroupChannelModel convertedChannel = ChannelGroupChannelModel
                        .builder()
                        .channel(channel)
                        .discordChannel(textChannelInGuild.orElse(null))
                        .build();
                convertedChannels.add(convertedChannel);
            });
            ChannelGroupModel channelGroup = ChannelGroupModel
                    .builder()
                    .name(group.getGroupName())
                    .typeKey(group.getChannelGroupType().getGroupTypeKey())
                    .channels(convertedChannels)
                    .build();
            converted.add(channelGroup);
        });
        return converted;
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
