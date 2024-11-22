package dev.sheldan.abstracto.repostdetection.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionFeatureDefinition;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionModuleDefinition;
import dev.sheldan.abstracto.repostdetection.converter.RepostCheckChannelModelConverter;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.repostdetection.model.template.RepostCheckChannelsModel;
import dev.sheldan.abstracto.repostdetection.service.RepostCheckChannelService;
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
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(SHOW_REPOST_CHECK_CHANNELS_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("showRepostCheckChannels")
                .module(RepostDetectionModuleDefinition.REPOST_DETECTION)
                .templated(true)
                .messageCommandOnly(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RepostDetectionFeatureDefinition.REPOST_DETECTION;
    }
}
