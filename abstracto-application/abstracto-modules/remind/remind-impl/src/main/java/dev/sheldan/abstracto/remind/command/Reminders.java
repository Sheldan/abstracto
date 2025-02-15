package dev.sheldan.abstracto.remind.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.remind.config.RemindFeatureDefinition;
import dev.sheldan.abstracto.remind.config.RemindSlashCommandNames;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.database.ReminderParticipant;
import dev.sheldan.abstracto.remind.model.template.commands.ReminderDisplay;
import dev.sheldan.abstracto.remind.model.template.commands.RemindersModel;
import dev.sheldan.abstracto.remind.service.management.ReminderManagementService;
import dev.sheldan.abstracto.remind.service.management.ReminderParticipantManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Reminders extends AbstractConditionableCommand {

    public static final String REMINDERS_RESPONSE_TEMPLATE = "reminders_response";
    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ReminderParticipantManagementService reminderParticipantManagementService;

    @Autowired
    private UserManagementService userManagementService;

    private MessageToSend getServerReminders(Long serverId, Member member) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        List<Reminder> activeReminders = reminderManagementService.getActiveRemindersForUser(aUserInAServer);
        List<Reminder> joinedReminders = reminderParticipantManagementService.getActiveReminders(aUserInAServer)
                .stream()
                .map(ReminderParticipant::getReminder)
                .collect(Collectors.toList());
        List<ReminderDisplay> reminders = activeReminders
                .stream()
                .map(ReminderDisplay::fromReminder)
                .collect(Collectors.toList());
        reminders.addAll(joinedReminders
                .stream()
                .map(ReminderDisplay::fromReminder)
                .peek(reminderDisplay -> reminderDisplay.setJoined(true))
                .collect(Collectors.toList()));
        RemindersModel model = RemindersModel
                .builder()
                .reminders(reminders)
                .userDisplay(UserDisplay.fromUser(member.getUser()))
                .build();
        log.info("Showing {} reminders for user {} in server {}.", activeReminders.size(), aUserInAServer.getUserReference().getId(), serverId);
        return templateService.renderEmbedTemplate(REMINDERS_RESPONSE_TEMPLATE, model, serverId);
    }

    private MessageToSend getUserReminders(User user) {
        AUser aUser = userManagementService.loadOrCreateUser(user.getIdLong());
        List<Reminder> activeReminders = reminderManagementService.getActiveUserRemindersForUser(aUser);
        List<ReminderDisplay> reminders = activeReminders
                .stream()
                .map(ReminderDisplay::fromReminder)
                .collect(Collectors.toList());
        RemindersModel model = RemindersModel
                .builder()
                .reminders(reminders)
                .userDisplay(UserDisplay.fromUser(user))
                .build();
        return templateService.renderEmbedTemplate(REMINDERS_RESPONSE_TEMPLATE, model);
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        MessageToSend messageToSend;
        if(ContextUtils.isUserCommand(event)) {
            messageToSend = getUserReminders(event.getUser());
        } else {
            Member member = event.getMember();
            Long serverId = event.getGuild().getIdLong();
            messageToSend = getServerReminders(serverId, member);
        }
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
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
                .commandName("list")
                .build();

        return CommandConfiguration.builder()
                .name("reminders")
                .async(true)
                .slashCommandOnly(true)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return RemindFeatureDefinition.REMIND;
    }
}
