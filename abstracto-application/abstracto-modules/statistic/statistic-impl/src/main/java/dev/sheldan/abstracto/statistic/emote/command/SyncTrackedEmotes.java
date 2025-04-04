package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private InteractionService interactionService;

    static final String SYNC_TRACKED_EMOTES_RESULT_RESPONSE = "syncTrackedEmotes_result_response";
    private static final String SYNC_TRACKED_EMOTES_COMMAND_NAME = "syncTrackedEmotes";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        TrackedEmoteSynchronizationResult syncResult = trackedEmoteService.synchronizeTrackedEmotes(event.getGuild());
        // show a result of how many emotes were deleted/added
        return interactionService.replyEmbed(SYNC_TRACKED_EMOTES_RESULT_RESPONSE, syncResult, event)
            .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(StatisticSlashCommandNames.STATISTIC_INTERNAL)
            .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
            .groupName("manage")
            .commandName("synctrackedemotes")
            .build();

        return CommandConfiguration.builder()
                .name(SYNC_TRACKED_EMOTES_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .slashCommandOnly(true)
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
