package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.Utility;
import dev.sheldan.abstracto.utility.config.UtilityFeatures;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.RemindersModel;
import dev.sheldan.abstracto.utility.service.management.ReminderManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Reminders extends AbstractConditionableCommand {

    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Reminder> activeReminders = reminderManagementService.getActiveRemindersForUser(commandContext.getUserInitiatedContext().getAUserInAServer());
        RemindersModel model = (RemindersModel) ContextConverter.fromCommandContext(commandContext, RemindersModel.class);
        model.setReminders(activeReminders);
        MessageToSend messageToSend = templateService.renderEmbedTemplate("reminders_response", model);
        channelService.sendMessageToEndInTextChannel(messageToSend, commandContext.getChannel());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().longHelp("Shows the current active reminders").usage("reminders").build();
        return CommandConfiguration.builder()
                .name("reminders")
                .module(Utility.UTILITY)
                .description("Shows the current active reminders")
                .causesReaction(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return UtilityFeatures.REMIND;
    }
}
