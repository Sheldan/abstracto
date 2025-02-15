package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
    private FeatureModeService featureModeService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    public static final String SHOW_TRACKED_EMOTES_STATIC_RESPONSE = "showTrackedEmotes_static_response";
    public static final String SHOW_TRACKED_EMOTES_ANIMATED_RESPONSE = "showTrackedEmotes_animated_response";
    public static final String SHOW_TRACKED_EMOTES_EXTERNAL_ANIMATED_RESPONSE = "showTrackedEmotes_external_animated_response";
    public static final String SHOW_TRACKED_EMOTES_EXTERNAL_STATIC_RESPONSE = "showTrackedEmotes_external_static_response";
    public static final String SHOW_TRACKED_EMOTES_DELETED_STATIC_RESPONSE = "showTrackedEmotes_deleted_static_response";
    public static final String SHOW_TRACKED_EMOTES_DELETED_ANIMATED_RESPONSE = "showTrackedEmotes_deleted_animated_response";
    public static final String SHOW_TRACKED_EMOTES_NO_STATS_AVAILABLE = "showTrackedEmotes_no_emotes_available";

    private static final String SHOW_TRACKED_EMOTES_COMMAND_NAME = "showTrackedEmotes";
    private static final String SHOW_TRACKED_EMOTES_SHOW_ALL = "showAll";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        // per default, do not show TrackedEmote for which tracking has been disabled
        Boolean showTrackingDisabled = false;
        if(slashCommandParameterService.hasCommandOption(SHOW_TRACKED_EMOTES_SHOW_ALL, event)) {
            showTrackingDisabled = slashCommandParameterService.getCommandOption(SHOW_TRACKED_EMOTES_SHOW_ALL, event, Boolean.class);
        }

        TrackedEmoteOverview trackedEmoteOverview = trackedEmoteService.loadTrackedEmoteOverview(event.getGuild(), showTrackingDisabled);
        List<CompletableFuture<Message>> messagePromises = new ArrayList<>();
        return event.deferReply().submit().thenCompose(interactionHook -> {
            boolean noTrackedEmotesAvailable = true;
            // only show the embed, if there are static tracked emotes
            if(!trackedEmoteOverview.getStaticEmotes().isEmpty()) {
                noTrackedEmotesAvailable = false;
                messagePromises.addAll(interactionService.sendMessageToInteraction(SHOW_TRACKED_EMOTES_STATIC_RESPONSE, trackedEmoteOverview, interactionHook));
            }

            // only show the embed if there are animated tracked emotes
            if(!trackedEmoteOverview.getAnimatedEmotes().isEmpty()) {
                noTrackedEmotesAvailable = false;
                messagePromises.addAll(interactionService.sendMessageToInteraction(SHOW_TRACKED_EMOTES_ANIMATED_RESPONSE, trackedEmoteOverview, interactionHook));
            }

            // only show the embed, if there are deleted static emotes
            if(!trackedEmoteOverview.getDeletedStaticEmotes().isEmpty()) {
                noTrackedEmotesAvailable = false;
                messagePromises.addAll(interactionService.sendMessageToInteraction(SHOW_TRACKED_EMOTES_DELETED_STATIC_RESPONSE, trackedEmoteOverview, interactionHook));
            }

            // only show the embed, if there are deleted animated emotes
            if(!trackedEmoteOverview.getDeletedAnimatedEmotes().isEmpty()) {
                noTrackedEmotesAvailable = false;
                messagePromises.addAll(interactionService.sendMessageToInteraction(SHOW_TRACKED_EMOTES_DELETED_ANIMATED_RESPONSE, trackedEmoteOverview, interactionHook));
            }

            boolean externalTrackingEnabled = featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, event.getGuild().getIdLong(), EmoteTrackingMode.EXTERNAL_EMOTES);

            // only show external emotes if external emotes are enabled
            if(externalTrackingEnabled) {

                // only show the embed if there are external static emotes
                if(!trackedEmoteOverview.getExternalStaticEmotes().isEmpty()) {
                    noTrackedEmotesAvailable = false;
                    messagePromises.addAll(interactionService.sendMessageToInteraction(SHOW_TRACKED_EMOTES_EXTERNAL_STATIC_RESPONSE, trackedEmoteOverview, interactionHook));
                }

                // only show the embed if there are external animated emotes
                if(!trackedEmoteOverview.getExternalAnimatedEmotes().isEmpty()) {
                    noTrackedEmotesAvailable = false;
                    messagePromises.addAll(interactionService.sendMessageToInteraction(SHOW_TRACKED_EMOTES_EXTERNAL_ANIMATED_RESPONSE, trackedEmoteOverview, interactionHook));
                }
            }

            // if there are no tracked emotes available, show an embed indicating so
            if(noTrackedEmotesAvailable) {
                messagePromises.addAll(interactionService.sendMessageToInteraction(SHOW_TRACKED_EMOTES_NO_STATS_AVAILABLE, new Object(), interactionHook));
            }
            return FutureUtils.toSingleFutureGeneric(messagePromises);
        }).thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();
        Parameter showAllParameter = Parameter
                .builder()
                .name(SHOW_TRACKED_EMOTES_SHOW_ALL)
                .templated(true)
                .optional(true)
                .type(Boolean.class)
                .build();
        parameters.add(showAllParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(StatisticSlashCommandNames.STATISTIC)
            .groupName("show")
            .commandName("trackedemotes")
            .build();

        return CommandConfiguration.builder()
                .name(SHOW_TRACKED_EMOTES_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .slashCommandOnly(true)
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
