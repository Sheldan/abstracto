package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emote.service.UsedEmoteService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
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
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    public static final String EMOTE_STATS_STATIC_RESPONSE = "emoteStats_static_response";
    public static final String EMOTE_STATS_ANIMATED_RESPONSE = "emoteStats_animated_response";
    public static final String EMOTE_STATS_NO_STATS_AVAILABLE = "emoteStats_no_stats_available";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // default is 1.1.1970
        Instant statsSince = Instant.EPOCH;
        if(!parameters.isEmpty()) {
            // subtract the given Duration from the current point in time, if there is any
            Duration duration = (Duration) parameters.get(0);
            statsSince = Instant.now().minus(duration);
        }
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        EmoteStatsModel emoteStatsModel = usedEmoteService.getActiveEmoteStatsForServerSince(server, statsSince);
        List<CompletableFuture<Message>> messagePromises = new ArrayList<>();
        // only show embed if static emote stats are available
        if(!emoteStatsModel.getStaticEmotes().isEmpty()) {
            log.debug("Emote stats has {} static emotes since {}.", emoteStatsModel.getStaticEmotes().size(), statsSince);
            messagePromises.addAll(channelService.sendEmbedTemplateInMessageChannel(EMOTE_STATS_STATIC_RESPONSE, emoteStatsModel, commandContext.getChannel()));
        }
        // only show embed if animated emote stats are available
        if(!emoteStatsModel.getAnimatedEmotes().isEmpty()) {
            log.debug("Emote stats has {} animated emotes since {}.", emoteStatsModel.getAnimatedEmotes(), statsSince);
            messagePromises.addAll(channelService.sendEmbedTemplateInMessageChannel(EMOTE_STATS_ANIMATED_RESPONSE, emoteStatsModel, commandContext.getChannel()));
        }
        // show an embed if no emote stats are available indicating so
        if(!emoteStatsModel.areStatsAvailable()) {
            log.info("No emote stats available for guild {} since {}.", commandContext.getGuild().getIdLong(), statsSince);
            messagePromises.addAll(channelService.sendEmbedTemplateInMessageChannel(EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), commandContext.getChannel()));
        }

        return FutureUtils.toSingleFutureGeneric(messagePromises)
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
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
                .name("emoteStats")
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .messageCommandOnly(true)
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
