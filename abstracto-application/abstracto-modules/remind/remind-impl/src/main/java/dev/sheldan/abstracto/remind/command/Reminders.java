package dev.sheldan.abstracto.remind.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.remind.config.RemindFeatureDefinition;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.template.commands.ReminderDisplay;
import dev.sheldan.abstracto.remind.model.template.commands.RemindersModel;
import dev.sheldan.abstracto.remind.service.management.ReminderManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class Reminders extends AbstractConditionableCommand {

    public static final String REMINDERS_RESPONSE_TEMPLATE = "reminders_response";
    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(commandContext.getAuthor());
        List<Reminder> activeReminders = reminderManagementService.getActiveRemindersForUser(aUserInAServer);
        RemindersModel model = (RemindersModel) ContextConverter.fromCommandContext(commandContext, RemindersModel.class);
        activeReminders.forEach(reminder -> {
            ServerChannelMessage originMessage = ServerChannelMessage
                    .builder()
                    .messageId(reminder.getMessageId())
                    .channelId(reminder.getChannel().getId())
                    .serverId(commandContext.getGuild().getIdLong())
                    .build();
            ReminderDisplay display = ReminderDisplay
                    .builder()
                    .reminder(reminder)
                    .message(originMessage)
                    .build();
            model.getReminders().add(display);
        });
        log.info("Showing {} reminders for user {} in server {}.", activeReminders.size(), commandContext.getAuthor().getId(), commandContext.getGuild().getId());
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(REMINDERS_RESPONSE_TEMPLATE, model, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("reminders")
                .async(true)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
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
