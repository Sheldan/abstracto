package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.config.StatisticModule;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowTrackedEmotes extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private FeatureModeService featureModeService;

    public static final String EMOTE_STATS_STATIC_RESPONSE = "showTrackedEmotes_static_response";
    public static final String EMOTE_STATS_ANIMATED_RESPONSE = "showTrackedEmotes_animated_response";
    public static final String EMOTE_STATS_EXTERNAL_ANIMATED_RESPONSE = "showTrackedEmotes_external_animated_response";
    public static final String EMOTE_STATS_EXTERNAL_STATIC_RESPONSE = "showTrackedEmotes_external_static_response";
    public static final String EMOTE_STATS_DELETED_STATIC_RESPONSE = "showTrackedEmotes_deleted_static_response";
    public static final String EMOTE_STATS_DELETED_ANIMATED_RESPONSE = "showTrackedEmotes_deleted_animated_response";
    public static final String EMOTE_STATS_NO_STATS_AVAILABLE = "showTrackedEmotes_no_emotes_available";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        checkParameters(commandContext);

        Boolean showTrackingDisabled = false;
        if(!commandContext.getParameters().getParameters().isEmpty()) {
            showTrackingDisabled = (Boolean) commandContext.getParameters().getParameters().get(0);
        }

        boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatures.EMOTE_TRACKING, commandContext.getGuild().getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);

        TrackedEmoteOverview trackedEmoteOverview = trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), showTrackingDisabled);
        boolean noStatsAvailable = true;
        List<CompletableFuture<Message>> messagePromises = new ArrayList<>();
        if(!trackedEmoteOverview.getStaticEmotes().isEmpty()) {
            noStatsAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_STATIC_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }
        if(!trackedEmoteOverview.getAnimatedEmotes().isEmpty()) {
            noStatsAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_ANIMATED_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }
        if(!trackedEmoteOverview.getDeletedStaticEmotes().isEmpty()) {
            noStatsAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_DELETED_STATIC_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }
        if(!trackedEmoteOverview.getDeletedAnimatedEmotes().isEmpty()) {
            noStatsAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_DELETED_ANIMATED_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }
        if(externalTrackingEnabled) {
            if(!trackedEmoteOverview.getExternalStaticEmotes().isEmpty()) {
                noStatsAvailable = false;
                messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_EXTERNAL_STATIC_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
            }
            if(!trackedEmoteOverview.getExternalAnimatedEmotes().isEmpty()) {
                noStatsAvailable = false;
                messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_EXTERNAL_ANIMATED_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
            }
        }
        if(noStatsAvailable) {
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), commandContext.getChannel()));
        }
        return FutureUtils.toSingleFutureGeneric(messagePromises)
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        parameters.add(Parameter.builder().name("showAll").templated(true).optional(true).type(Boolean.class).build());
        return CommandConfiguration.builder()
                .name("showTrackedEmotes")
                .module(StatisticModule.STATISTIC)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return StatisticFeatures.EMOTE_TRACKING;
    }
}
