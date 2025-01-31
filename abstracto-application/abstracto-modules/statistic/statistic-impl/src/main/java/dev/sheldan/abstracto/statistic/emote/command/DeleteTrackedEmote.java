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
 * This command completely deletes one individual {@link TrackedEmote} and all of its usages from the database. This command cannot be undone.
 */
@Component
public class DeleteTrackedEmote extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private SlashCommandService slashCommandService;

    private static final String DELETE_TRACKED_EMOTE_TRACKED_EMOTE = "trackedEmote";
    private static final String DELETE_TRACKED_EMOTE_COMMAND_NAME = "deleteTrackedEmote";
    private static final String DELETE_TRACKED_EMOTE_RESPONSE = "deleteTrackedEmote_response";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String emote = slashCommandParameterService.getCommandOption(DELETE_TRACKED_EMOTE_TRACKED_EMOTE, event, String.class);
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
        ServerSpecificId serverEmoteId = new ServerSpecificId(event.getGuild().getIdLong(), emoteId);
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(serverEmoteId);
        trackedEmoteService.deleteTrackedEmote(trackedEmote);
        return slashCommandService.completeConfirmableCommand(event, DELETE_TRACKED_EMOTE_RESPONSE);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter trackedEmoteParameter = Parameter
                .builder()
                .name(DELETE_TRACKED_EMOTE_TRACKED_EMOTE)
                .templated(true)
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
            .groupName("manage")
            .commandName("deletetrackedemote")
            .build();

        return CommandConfiguration.builder()
                .name(DELETE_TRACKED_EMOTE_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .requiresConfirmation(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }
}
