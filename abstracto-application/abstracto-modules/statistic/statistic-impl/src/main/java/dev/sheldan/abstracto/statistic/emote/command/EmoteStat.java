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
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.command.parameter.UsedEmoteTypeParameter;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResultDisplay;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.UsedEmoteService;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.statistic.emote.command.EmoteStats.EMOTE_STATS_NO_STATS_AVAILABLE;

@Component
@Slf4j
public class EmoteStat extends AbstractConditionableCommand {

    @Autowired
    private UsedEmoteService usedEmoteService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    private static final String EMOTE_STAT_RESPONSE = "emoteStat_response";

    private static final String EMOTE_STAT_USED_EMOTE_TYPE = "type";
    private static final String EMOTE_STAT_DURATION = "period";
    private static final String EMOTE_STAT_TRACKED_EMOTE = "trackedEmote";
    private static final String EMOTE_STAT_COMMAND_NAME = "emoteStat";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        UsedEmoteTypeParameter typeEnum;
        if(slashCommandParameterService.hasCommandOption(EMOTE_STAT_USED_EMOTE_TYPE, event)) {
            String type = slashCommandParameterService.getCommandOption(EMOTE_STAT_USED_EMOTE_TYPE, event, String.class);
            typeEnum = UsedEmoteTypeParameter.valueOf(type);
        } else {
            typeEnum = null;
        }
        Instant startTime;
        if(slashCommandParameterService.hasCommandOption(EMOTE_STAT_DURATION, event)) {
            String durationString = slashCommandParameterService.getCommandOption(EMOTE_STAT_DURATION, event, Duration.class, String.class);
            Duration durationSince = ParseUtils.parseDuration(durationString);
            startTime = Instant.now().minus(durationSince);
        } else {
            startTime = Instant.EPOCH;
        }
        String emote = slashCommandParameterService.getCommandOption(EMOTE_STAT_TRACKED_EMOTE, event, String.class);
        Emoji emoji = slashCommandParameterService.loadEmoteFromString(emote, event.getGuild());
        if(emoji instanceof CustomEmoji) {
            Long emoteId = ((CustomEmoji) emoji).getIdLong();
            return showResponse(event, emoteId, startTime, typeEnum);
        } else if(StringUtils.isNumeric(emote)) {
            return showResponse(event, Long.parseLong(emote), startTime, typeEnum);
        } else {
            throw new TrackedEmoteNotFoundException();
        }
    }

    private CompletableFuture<CommandResult> showResponse(SlashCommandInteractionEvent event, Long emoteId, Instant startTime,
                                                                               UsedEmoteTypeParameter typeEnum)
    {
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(new ServerSpecificId(event.getGuild().getIdLong(), emoteId));
        EmoteStatsResultDisplay emoteStatsModel = usedEmoteService.getEmoteStatForEmote(trackedEmote,
            startTime, UsedEmoteTypeParameter.convertToUsedEmoteType(typeEnum));
        if(emoteStatsModel.getResult().getAmount() == null) {
            return interactionService.replyEmbed(EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), event)
                .thenApply(unused -> CommandResult.fromIgnored());
        }
        return interactionService.replyEmbed(EMOTE_STAT_RESPONSE, emoteStatsModel, event)
            .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter trackedEmoteParameter = Parameter
                .builder()
                .name(EMOTE_STAT_TRACKED_EMOTE)
                .templated(true)
                .type(TrackedEmote.class)
                .build();
        parameters.add(trackedEmoteParameter);

        Parameter periodParameter = Parameter
                .builder()
                .name(EMOTE_STAT_DURATION)
                .templated(true)
                .optional(true)
                .type(Duration.class)
                .build();
        parameters.add(periodParameter);

        List<String> emoteTypes = Arrays
            .stream(UsedEmoteTypeParameter.values())
            .map(Enum::name)
            .collect(Collectors.toList());

        Parameter typeParameter = Parameter
            .builder()
            .name(EMOTE_STAT_USED_EMOTE_TYPE)
            .templated(true)
            .slashCommandOnly(true)
            .optional(true)
            .choices(emoteTypes)
            .type(String.class)
            .build();

        parameters.add(typeParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(StatisticSlashCommandNames.STATISTIC)
            .groupName("emotestats")
            .commandName("singular")
            .build();

        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();
        return CommandConfiguration.builder()
                .name(EMOTE_STAT_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
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
