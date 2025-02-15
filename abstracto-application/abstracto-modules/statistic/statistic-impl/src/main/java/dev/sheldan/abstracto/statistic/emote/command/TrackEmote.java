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
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.command.parameter.TrackEmoteParameter;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This command can be used to track one individual {@link TrackedEmote} newly, or set the emote to be tracked again.
 * This can either be done via providing the emote or via ID.
 */
@Component
public class TrackEmote extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    private static final String TRACK_EMOTE_COMMAND_NAME = "trackEmote";
    private static final String TRACK_EMOTE_EMOTE = "emote";

    private static final String TRACK_EMOTE_RESPONSE = "trackEmote_response";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String emote = slashCommandParameterService.getCommandOption(TRACK_EMOTE_EMOTE, event, String.class);
        Emoji emoji = slashCommandParameterService.loadEmoteFromString(emote, event.getGuild());
        if(emoji instanceof CustomEmoji customEmoji) {
            Long emoteId = customEmoji.getIdLong();
            long serverId = event.getGuild().getIdLong();
            // if its already a tracked emote, just set the tracking_enabled flag to true
            if(trackedEmoteManagementService.trackedEmoteExists(emoteId, serverId)) {
                TrackedEmote trackedemote = trackedEmoteManagementService.loadByEmoteId(emoteId, serverId);
                trackedEmoteManagementService.enableTrackedEmote(trackedemote);
                return interactionService.replyEmbed(TRACK_EMOTE_RESPONSE, event)
                    .thenApply(interactionHook -> CommandResult.fromIgnored());
            } else {
                // if its a new emote, lets see if its external
                boolean external = !emoteService.emoteIsFromGuild(customEmoji, event.getGuild());
                if (external) {
                    // this throws an exception if the feature mode is not enabled
                    featureModeService.validateActiveFeatureMode(serverId, StatisticFeatureDefinition.EMOTE_TRACKING, EmoteTrackingMode.EXTERNAL_EMOTES);
                }
                trackedEmoteService.createTrackedEmote(customEmoji, event.getGuild(), external);
                return interactionService.replyEmbed(TRACK_EMOTE_RESPONSE, event)
                    .thenApply(interactionHook -> CommandResult.fromIgnored());
            }
        } else {
            throw new TrackedEmoteNotFoundException();
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter emoteParameter = Parameter
                .builder()
                .name(TRACK_EMOTE_EMOTE)
                .templated(true)
                .type(TrackEmoteParameter.class)
                .build();
        parameters.add(emoteParameter);

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
            .commandName("trackemote")
            .build();

        return CommandConfiguration.builder()
                .name(TRACK_EMOTE_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
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
}
