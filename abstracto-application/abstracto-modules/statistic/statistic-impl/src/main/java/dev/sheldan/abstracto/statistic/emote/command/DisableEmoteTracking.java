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
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This command disables the tracking for either one {@link TrackedEmote} or for all of them, if no emote is given as a parameter
 */
@Component
public class DisableEmoteTracking extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    private static final String DISABLE_EMOTE_TRACKING_COMMAND_NAME = "disableEmoteTracking";
    private static final String DISABLE_EMOTE_TRACKING_TRACKED_EMOTE = "trackedEmote";
    private static final String DISABLE_EMOTE_TRACKING_RESPONSE = "disableEmoteTracking_response";


    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        if(slashCommandParameterService.hasCommandOption(DISABLE_EMOTE_TRACKING_TRACKED_EMOTE, event)) {
            String emote = slashCommandParameterService.getCommandOption(DISABLE_EMOTE_TRACKING_TRACKED_EMOTE, event, String.class);
            Emoji emoji = slashCommandParameterService.loadEmoteFromString(emote, event.getGuild());
            if(emoji instanceof CustomEmoji) {
                Long emoteId = ((CustomEmoji) emoji).getIdLong();
                disableTracking(event, emoteId);
            } else if(StringUtils.isNumeric(emote)) {
                disableTracking(event, Long.parseLong(emote));
            } else {
                throw new TrackedEmoteNotFoundException();
            }

        } else {
            trackedEmoteService.disableEmoteTracking(event.getGuild());
        }
        return interactionService.replyEmbed(DISABLE_EMOTE_TRACKING_RESPONSE, event)
            .thenApply(interactionHook -> CommandResult.fromIgnored());
    }

    private void disableTracking(SlashCommandInteractionEvent event, Long emoteId) {
        ServerSpecificId serverEmoteId = new ServerSpecificId(event.getGuild().getIdLong(), emoteId);
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(serverEmoteId);
        trackedEmoteManagementService.disableTrackedEmote(trackedEmote);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter trackedEmoteParameter = Parameter
                .builder()
                .name(DISABLE_EMOTE_TRACKING_TRACKED_EMOTE)
                .templated(true)
                .optional(true)
                .type(TrackedEmote.class)
                .build();
        parameters.add(trackedEmoteParameter);

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
            .commandName("disableemotetracking")
            .build();

        return CommandConfiguration.builder()
                .name(DISABLE_EMOTE_TRACKING_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
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
