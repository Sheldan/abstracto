package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingModule;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.service.UsedEmoteService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
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
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    public static final String EMOTE_STATS_STATIC_EXTERNAL_RESPONSE = "externalEmoteStats_static_response";
    public static final String EMOTE_STATS_ANIMATED_EXTERNAL_RESPONSE = "externalEmoteStats_animated_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // default is 1.1.1970
        Instant statsSince = Instant.EPOCH;
        if(!parameters.isEmpty()) {
            // subtract the given Duration parameter from the current point in time
            Duration duration = (Duration) parameters.get(0);
            statsSince = Instant.now().minus(duration);
        }
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        EmoteStatsModel emoteStatsModel = usedEmoteService.getExternalEmoteStatsForServerSince(server, statsSince);
        List<CompletableFuture<Message>> messagePromises = new ArrayList<>();

        // only show embed if static emote stats are available
        if(!emoteStatsModel.getStaticEmotes().isEmpty()) {
            log.trace("External emote stats has {} static emotes since {}.", emoteStatsModel.getStaticEmotes().size(), statsSince);
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_STATIC_EXTERNAL_RESPONSE, emoteStatsModel, commandContext.getChannel()));
        }

        // only show embed if animated emote stats are available
        if(!emoteStatsModel.getAnimatedEmotes().isEmpty()) {
            log.trace("External emote stats has {} animated emotes since {}.", emoteStatsModel.getAnimatedEmotes(), statsSince);
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EMOTE_STATS_ANIMATED_EXTERNAL_RESPONSE, emoteStatsModel, commandContext.getChannel()));
        }

        // show an embed if no emote stats are available indicating so
        if(!emoteStatsModel.areStatsAvailable()) {
            log.info("No external emote stats available for guild {} since {}.", commandContext.getGuild().getIdLong(), statsSince);
            messagePromises.addAll(channelService.sendEmbedTemplateInChannel(EmoteStats.EMOTE_STATS_NO_STATS_AVAILABLE, new Object(), commandContext.getChannel()));
        }

        return FutureUtils.toSingleFutureGeneric(messagePromises)
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("period").templated(true).optional(true).type(Duration.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("externalEmoteStats")
                .module(EmoteTrackingModule.EMOTE_TRACKING)
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

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(EmoteTrackingMode.EXTERNAL_EMOTES);
    }
}
