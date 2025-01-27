package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This command removes all {@link dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote} instances
 * and all {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote} in a guild. This command cannot be undone.
 */
@Component
public class ResetEmoteStats extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private SlashCommandService slashCommandService;

    private static final String RESET_EMOTE_STATS_COMMAND_NAME = "resetEmoteStats";
    private static final String RESET_EMOTE_STATS_RESPONSE = "resetEmoteStats_response";

    @Override
    public CommandResult execute(CommandContext commandContext) {
        trackedEmoteService.resetEmoteStats(commandContext.getGuild());
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        trackedEmoteService.resetEmoteStats(event.getGuild());
        return slashCommandService.completeConfirmableCommand(event, RESET_EMOTE_STATS_RESPONSE);
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
            .groupName("manage")
            .commandName("resetemotestats")
            .build();

        return CommandConfiguration.builder()
                .name(RESET_EMOTE_STATS_COMMAND_NAME)
                .messageCommandOnly(true)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
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
