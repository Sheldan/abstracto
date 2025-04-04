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
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.command.parameter.UsedEmoteTypeParameter;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emote.service.UsedEmoteService;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This command displays the emote stats for the current {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote} within the
 * {@link net.dv8tion.jda.api.entities.Guild} the command has been executed in
 */
@Component
@Slf4j
public class EmoteStats extends AbstractConditionableCommand {

    @Autowired
    private UsedEmoteService usedEmoteService;

    @Autowired
    private PaginatorService paginatorService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    public static final String EMOTE_STATS_STATIC_RESPONSE = "emoteStats_static_response";
    public static final String EMOTE_STATS_ANIMATED_RESPONSE = "emoteStats_animated_response";
    public static final String EMOTE_STATS_NO_STATS_AVAILABLE = "emoteStats_no_stats_available";
    private static final String EMOTE_STATS_USED_EMOTE_TYPE = "type";
    private static final String EMOTE_STATS_DURATION = "period";
    private static final String EMOTE_STATS_COMMAND_NAME = "emoteStats";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        UsedEmoteTypeParameter typeEnum;
        if(slashCommandParameterService.hasCommandOption(EMOTE_STATS_USED_EMOTE_TYPE, event)) {
            String type = slashCommandParameterService.getCommandOption(EMOTE_STATS_USED_EMOTE_TYPE, event, String.class);
            typeEnum = UsedEmoteTypeParameter.valueOf(type);
        } else {
            typeEnum = null;
        }
        Instant startTime;
        if(slashCommandParameterService.hasCommandOption(EMOTE_STATS_DURATION, event)) {
            String durationString = slashCommandParameterService.getCommandOption(EMOTE_STATS_DURATION, event, Duration.class, String.class);
            Duration durationSince = ParseUtils.parseDuration(durationString);
            startTime = Instant.now().minus(durationSince);
        } else {
            startTime = Instant.EPOCH;
        }
        AServer server = serverManagementService.loadServer(event.getGuild());
        EmoteStatsModel emoteStatsModel = usedEmoteService.getActiveEmoteStatsForServerSince(server, startTime, UsedEmoteTypeParameter.convertToUsedEmoteType(typeEnum));
        List<CompletableFuture<Void>> messagePromises = new ArrayList<>();
        return event.deferReply().submit().thenCompose(interactionHook -> {
            // only show embed if static emote stats are available
            if(!emoteStatsModel.getStaticEmotes().isEmpty()) {
                log.debug("Emote stats has {} static emotes since {}.", emoteStatsModel.getStaticEmotes().size(), startTime);
                messagePromises.add(paginatorService.sendPaginatorToInteraction(EMOTE_STATS_STATIC_RESPONSE, emoteStatsModel, interactionHook));
            }
            // only show embed if animated emote stats are available
            if(!emoteStatsModel.getAnimatedEmotes().isEmpty()) {
                log.debug("Emote stats has {} animated emotes since {}.", emoteStatsModel.getAnimatedEmotes(), startTime);
                messagePromises.add(paginatorService.sendPaginatorToInteraction(EMOTE_STATS_ANIMATED_RESPONSE, emoteStatsModel, interactionHook));
            }
            // show an embed if no emote stats are available indicating so
            if(!emoteStatsModel.areStatsAvailable()) {
                log.info("No emote stats available for guild {} since {}.", event.getGuild().getIdLong(), startTime);
                return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), interactionHook))
                    .thenApply(unused -> CommandResult.fromSuccess());
            }
            return FutureUtils.toSingleFutureGeneric(messagePromises)
                .thenApply(unused -> CommandResult.fromIgnored());
        });
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter periodParameter = Parameter
                .builder()
                .name(EMOTE_STATS_DURATION)
                .templated(true)
                .optional(true)
                .type(Duration.class)
                .build();

        parameters.add(periodParameter);

        Parameter typeParameter = Parameter
            .builder()
            .name(EMOTE_STATS_USED_EMOTE_TYPE)
            .templated(true)
            .slashCommandOnly(true)
            .optional(true)
            .type(UsedEmoteTypeParameter.class)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(StatisticSlashCommandNames.STATISTIC)
            .groupName("emotestats")
            .commandName("current")
            .build();
        parameters.add(typeParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name(EMOTE_STATS_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .messageCommandOnly(true)
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
