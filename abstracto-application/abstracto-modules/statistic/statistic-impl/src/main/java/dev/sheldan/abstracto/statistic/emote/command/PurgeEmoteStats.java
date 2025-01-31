package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.UsedEmoteService;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This command will delete the instances of {@link dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote} of a given {@link TrackedEmote}
 * for the desired {@link Duration}, or all of them. This command cannot be undone.
 */
@Component
public class PurgeEmoteStats extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private UsedEmoteService usedEmoteService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private SlashCommandService slashCommandService;

    private static final String PURGE_EMOTE_STATS_COMMAND_NAME = "purgeEmoteStats";
    private static final String PURGE_EMOTE_STATS_TRACKED_EMOTE = "trackedEmote";
    private static final String PURGE_EMOTE_STATS_PERIOD = "period";
    private static final String PURGE_EMOTE_STATS_RESPONSE = "purgeEmoteStats_response";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String emote = slashCommandParameterService.getCommandOption(PURGE_EMOTE_STATS_TRACKED_EMOTE, event, String.class);
        Emoji emoji = slashCommandParameterService.loadEmoteFromString(emote, event.getGuild());
        if(emoji instanceof CustomEmoji) {
            Long emoteId = ((CustomEmoji) emoji).getIdLong();
            return createResponse(event, emoteId);
        } else if(StringUtils.isNumeric(emote)) {
            return createResponse(event, Long.parseLong(emote));
        } else {
            throw new TrackedEmoteNotFoundException();
        }
    }

    private CompletableFuture<CommandResult> createResponse(SlashCommandInteractionEvent event, Long emoteId) {
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(new ServerSpecificId(event.getGuild().getIdLong(), emoteId));
        // default 1.1.1970
        Instant since = Instant.EPOCH;
        if(slashCommandParameterService.hasCommandOption(PURGE_EMOTE_STATS_PERIOD, event)) {
            // if a Duration is given, subtract it from the current point in time
            String durationString = slashCommandParameterService.getCommandOption(PURGE_EMOTE_STATS_PERIOD, event, Duration.class, String.class);
            Duration duration = ParseUtils.parseDuration(durationString);
            since = Instant.now().minus(duration);
        }
        usedEmoteService.purgeEmoteUsagesSince(trackedEmote, since);
        return slashCommandService.completeConfirmableCommand(event, PURGE_EMOTE_STATS_RESPONSE);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter trackedEmoteParameter = Parameter
                .builder()
                .name(PURGE_EMOTE_STATS_TRACKED_EMOTE)
                .templated(true)
                .type(TrackedEmote.class)
                .build();
        parameters.add(trackedEmoteParameter);
        Parameter periodParameter = Parameter
                .builder()
                .name(PURGE_EMOTE_STATS_PERIOD)
                .templated(true)
                .optional(true)
                .type(Duration.class)
                .build();
        parameters.add(periodParameter);

        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(StatisticSlashCommandNames.STATISTIC_INTERNAL)
            .groupName("manage")
            .commandName("purgeemotestats")
            .build();

        return CommandConfiguration.builder()
                .name(PURGE_EMOTE_STATS_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .requiresConfirmation(true)
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
