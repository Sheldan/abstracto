package dev.sheldan.abstracto.remind.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.remind.config.RemindFeatureDefinition;
import dev.sheldan.abstracto.remind.config.RemindSlashCommandNames;
import dev.sheldan.abstracto.remind.service.ReminderService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class UnRemind extends AbstractConditionableCommand {

    private static final String UN_REMIND_COMMAND = "unRemind";
    private static final String REMINDER_ID_PARAMETER = "reminderId";
    private static final String UN_REMIND_RESPONSE = "unRemind_response";

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Long reminderId = (Long) commandContext.getParameters().getParameters().get(0);
        reminderService.unRemind(reminderId, userInServerManagementService.loadOrCreateUser(commandContext.getAuthor()));
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long reminderId = slashCommandParameterService.getCommandOption(REMINDER_ID_PARAMETER, event, Long.class, Integer.class).longValue();
        if(ContextUtils.isUserCommand(event)) {
            reminderService.unRemind(reminderId, userManagementService.loadOrCreateUser(event.getUser().getIdLong()));
        } else {
            reminderService.unRemind(reminderId, userInServerManagementService.loadOrCreateUser(event.getMember()));
        }
        return interactionService.replyEmbed(UN_REMIND_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter reminderParameter = Parameter
                .builder()
                .name(REMINDER_ID_PARAMETER)
                .templated(true)
                .type(Long.class)
                .build();
        List<Parameter> parameters = Arrays.asList(reminderParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(RemindSlashCommandNames.REMIND)
                .commandName("cancel")
                .build();

        return CommandConfiguration.builder()
                .name(UN_REMIND_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .supportsEmbedException(true)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RemindFeatureDefinition.REMIND;
    }
}
