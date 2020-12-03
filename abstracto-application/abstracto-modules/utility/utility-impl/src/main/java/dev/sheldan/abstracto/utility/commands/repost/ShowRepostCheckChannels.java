package dev.sheldan.abstracto.utility.commands.repost;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.RepostDetectionModuleInterface;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.converter.RepostCheckChannelModelConverter;
import dev.sheldan.abstracto.utility.models.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.utility.models.template.commands.RepostCheckChannelsModel;
import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowRepostCheckChannels extends AbstractConditionableCommand {

    public static final String SHOW_REPOST_CHECK_CHANNELS_RESPONSE_TEMPLATE_KEY = "showRepostCheckChannels_response";
    @Autowired
    private RepostCheckChannelModelConverter converter;

    @Autowired
    private RepostCheckChannelService checkChannelService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<RepostCheckChannelGroup> channelGroups = checkChannelService.getChannelGroupsWithEnabledCheck(commandContext.getGuild().getIdLong());
        RepostCheckChannelsModel model = converter.fromRepostCheckChannelGroups(channelGroups, commandContext.getGuild());
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(SHOW_REPOST_CHECK_CHANNELS_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("showRepostCheckChannels")
                .module(RepostDetectionModuleInterface.REPOST_DETECTION)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REPOST_DETECTION;
    }
}
