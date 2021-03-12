package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This command gives an overview over all {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote} in a guild.
 * It will not show external emotes, if the feature mode EmoteTrackingMode.EXTERNAL_EMOTES is disabled. There is a parameter to also show
 * emotes for which the tracking has been disabled
 */
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

        // per default, do not show TrackedEmote for which tracking has been disabled
        Boolean showTrackingDisabled = false;
        if(!commandContext.getParameters().getParameters().isEmpty()) {
            showTrackingDisabled = (Boolean) commandContext.getParameters().getParameters().get(0);
        }

        TrackedEmoteOverview trackedEmoteOverview = trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), showTrackingDisabled);
        boolean noTrackedEmotesAvailable = true;
        List<CompletableFuture<Message>> messagePromises = new ArrayList<>();
        // only show the embed, if there are static tracked emotes
        if(!trackedEmoteOverview.getStaticEmotes().isEmpty()) {
            noTrackedEmotesAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_STATIC_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }

        // only show the embed if there are animated tracked emotes
        if(!trackedEmoteOverview.getAnimatedEmotes().isEmpty()) {
            noTrackedEmotesAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_ANIMATED_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }

        // only show the embed, if there are deleted static emotes
        if(!trackedEmoteOverview.getDeletedStaticEmotes().isEmpty()) {
            noTrackedEmotesAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_DELETED_STATIC_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }

        // only show the embed, if there are deleted animated emotes
        if(!trackedEmoteOverview.getDeletedAnimatedEmotes().isEmpty()) {
            noTrackedEmotesAvailable = false;
            messagePromises.addAll(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_DELETED_ANIMATED_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
        }

        boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, commandContext.getGuild().getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);

        // only show external emotes if external emotes are enabled
        if(externalTrackingEnabled) {

            // only show the embed if there are external static emotes
            if(!trackedEmoteOverview.getExternalStaticEmotes().isEmpty()) {
                noTrackedEmotesAvailable = false;
                messagePromises.addAll(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_EXTERNAL_STATIC_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
            }

            // only show the embed if there are external animated emotes
            if(!trackedEmoteOverview.getExternalAnimatedEmotes().isEmpty()) {
                noTrackedEmotesAvailable = false;
                messagePromises.addAll(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_EXTERNAL_ANIMATED_RESPONSE, trackedEmoteOverview, commandContext.getChannel()));
            }
        }

        // if there are no tracked emotes available, show an embed indicating so
        if(noTrackedEmotesAvailable) {
            messagePromises.addAll(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), commandContext.getChannel()));
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
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }
}
