package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResultDisplay;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.UsedEmoteService;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import lombok.extern.slf4j.Slf4j;
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
    private ChannelService channelService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    public static final String EMOTE_STAT_RESPONSE = "emoteStat_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // default is 1.1.1970
        Instant statsSince = Instant.EPOCH;
        TrackedEmote emote = (TrackedEmote) parameters.get(0);
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(emote.getTrackedEmoteId());
        if(parameters.size() == 2) {
            // subtract the given Duration from the current point in time, if there is any
            Duration duration = (Duration) parameters.get(1);
            statsSince = Instant.now().minus(duration);
        }
        EmoteStatsResultDisplay emoteStatsModel = usedEmoteService.getEmoteStatForEmote(trackedEmote, statsSince);
        if(emoteStatsModel.getResult().getAmount() == null) {
            return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromIgnored());
        }
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInMessageChannel(EMOTE_STAT_RESPONSE, emoteStatsModel, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter trackedEmoteParameter = Parameter
                .builder()
                .name("trackedEmote")
                .templated(true)
                .type(TrackedEmote.class)
                .build();
        parameters.add(trackedEmoteParameter);
        Parameter periodParameter = Parameter
                .builder()
                .name("period")
                .templated(true)
                .optional(true)
                .type(Duration.class)
                .build();
        parameters.add(periodParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("emoteStat")
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .async(true)
                .messageCommandOnly(true)
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
