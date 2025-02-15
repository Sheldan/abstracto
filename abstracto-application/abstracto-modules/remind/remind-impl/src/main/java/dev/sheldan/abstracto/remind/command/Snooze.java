package dev.sheldan.abstracto.remind.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.remind.config.RemindFeatureDefinition;
import dev.sheldan.abstracto.remind.config.RemindSlashCommandNames;
import dev.sheldan.abstracto.remind.service.ReminderService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Snooze extends AbstractConditionableCommand {

    private static final String SNOOZE_COMMAND = "snooze";
    private static final String DURATION_PARAMETER = "duration";
    private static final String REMINDER_ID_PARAMETER = "reminderId";
    private static final String SNOOZE_RESPONSE = "snooze_response";

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long reminderId = slashCommandParameterService.getCommandOption(REMINDER_ID_PARAMETER, event, Long.class, Integer.class).longValue();
        String newDurationString = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, String.class, String.class);
        Duration newDuration = ParseUtils.parseDuration(newDurationString);
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(event.getMember());
        reminderService.snoozeReminder(reminderId, aUserInAServer, newDuration);
        return interactionService.replyEmbed(SNOOZE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter reminderParameter = Parameter
                .builder()
                .name(REMINDER_ID_PARAMETER)
                .type(Long.class)
                .templated(true)
                .build();
        Parameter durationParameter = Parameter
                .builder()
                .name(DURATION_PARAMETER)
                .type(Duration.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(reminderParameter, durationParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(RemindSlashCommandNames.REMIND)
                .commandName(SNOOZE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SNOOZE_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RemindFeatureDefinition.REMIND;
    }
}
