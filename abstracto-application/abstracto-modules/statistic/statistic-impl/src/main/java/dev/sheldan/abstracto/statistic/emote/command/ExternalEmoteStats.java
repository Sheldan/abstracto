package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
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
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emote.service.UsedEmoteService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This command will show the emote statistics for all emotes which are tracked in the current server, but are not from that server.
 * There is an optional {@link Duration} parameter, which will define the amount of time to retrieve the stats for. If not provided, all stats will be shown.
 */
@Component
@Slf4j
public class ExternalEmoteStats extends AbstractConditionableCommand {

    @Autowired
    private UsedEmoteService usedEmoteService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private PaginatorService paginatorService;

    public static final String EMOTE_STATS_STATIC_EXTERNAL_RESPONSE = "externalEmoteStats_static_response";
    public static final String EMOTE_STATS_ANIMATED_EXTERNAL_RESPONSE = "externalEmoteStats_animated_response";
    private static final String EXTERNAL_EMOTE_STATS_USED_EMOTE_TYPE = "type";

    private static final String EXTERNAL_EMOTE_STATS_PERIOD = "period";
    private static final String EXTERNAL_EMOTE_STATS_COMMAND_NAME = "externalEmoteStats";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        UsedEmoteTypeParameter typeEnum;
        if(slashCommandParameterService.hasCommandOption(EXTERNAL_EMOTE_STATS_USED_EMOTE_TYPE, event)) {
            String type = slashCommandParameterService.getCommandOption(EXTERNAL_EMOTE_STATS_USED_EMOTE_TYPE, event, String.class);
            typeEnum = UsedEmoteTypeParameter.valueOf(type);
        } else {
            typeEnum = null;
        }
        Instant startTime;
        if(slashCommandParameterService.hasCommandOption(EXTERNAL_EMOTE_STATS_PERIOD, event)) {
            String durationString = slashCommandParameterService.getCommandOption(EXTERNAL_EMOTE_STATS_PERIOD, event, Duration.class, String.class);
            Duration durationSince = ParseUtils.parseDuration(durationString);
            startTime = Instant.now().minus(durationSince);
        } else {
            startTime = Instant.EPOCH;
        }

        AServer server = serverManagementService.loadServer(event.getGuild());
        EmoteStatsModel emoteStatsModel = usedEmoteService.getExternalEmoteStatsForServerSince(server, startTime, UsedEmoteTypeParameter.convertToUsedEmoteType(typeEnum));
        List<CompletableFuture<Void>> messagePromises = new ArrayList<>();
        return event.deferReply().submit().thenCompose(interactionHook -> {
            // only show embed if static emote stats are available
            if (!emoteStatsModel.getStaticEmotes().isEmpty()) {
                log.debug("External emote stats has {} static emotes since {}.", emoteStatsModel.getStaticEmotes().size(), startTime);
                messagePromises.add(paginatorService.sendPaginatorToInteraction(EMOTE_STATS_STATIC_EXTERNAL_RESPONSE, emoteStatsModel, interactionHook));
            }

            // only show embed if animated emote stats are available
            if (!emoteStatsModel.getAnimatedEmotes().isEmpty()) {
                log.debug("External emote stats has {} animated emotes since {}.", emoteStatsModel.getAnimatedEmotes(), startTime);
                messagePromises.add(paginatorService.sendPaginatorToInteraction(EMOTE_STATS_ANIMATED_EXTERNAL_RESPONSE, emoteStatsModel, interactionHook));
            }

            // show an embed if no emote stats are available indicating so
            if (!emoteStatsModel.areStatsAvailable()) {
                log.info("No external emote stats available for guild {} since {}.", event.getGuild().getIdLong(), startTime);
                return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(EmoteStats.EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), interactionHook))
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
                .name(EXTERNAL_EMOTE_STATS_PERIOD)
                .templated(true)
                .optional(true)
                .type(Duration.class)
                .build();
        parameters.add(periodParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        Parameter typeParameter = Parameter
            .builder()
            .name(EXTERNAL_EMOTE_STATS_USED_EMOTE_TYPE)
            .templated(true)
            .slashCommandOnly(true)
            .optional(true)
            .type(UsedEmoteTypeParameter.class)
            .build();

        parameters.add(typeParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(StatisticSlashCommandNames.STATISTIC)
            .groupName("emotestats")
            .commandName("external")
            .build();

        return CommandConfiguration.builder()
                .name(EXTERNAL_EMOTE_STATS_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .messageCommandOnly(true)
                .async(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
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

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(EmoteTrackingMode.EXTERNAL_EMOTES);
    }
}
