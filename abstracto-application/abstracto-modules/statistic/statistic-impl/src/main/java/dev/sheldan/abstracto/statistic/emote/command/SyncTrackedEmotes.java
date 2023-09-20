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
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This command can be used to synchronize the state of {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote}
 * in the database, with the state of emote in the {@link net.dv8tion.jda.api.entities.Guild}.
 * It will mark emotes not in the guild anymore and add emotes which are not yet tracked.
 */
@Component
public class SyncTrackedEmotes extends AbstractConditionableCommand {

    public static final String SYNC_TRACKED_EMOTES_RESULT_RESPONSE = "syncTrackedEmotes_result_response";
    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        TrackedEmoteSynchronizationResult syncResult = trackedEmoteService.synchronizeTrackedEmotes(commandContext.getGuild());
        // show a result of how many emotes were deleted/added
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(SYNC_TRACKED_EMOTES_RESULT_RESPONSE, syncResult, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("syncTrackedEmotes")
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
