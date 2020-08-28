package dev.sheldan.abstracto.utility.commands.remind;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.RemindersModel;
import dev.sheldan.abstracto.utility.service.management.ReminderManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Reminders extends AbstractConditionableCommand {

    public static final String REMINDERS_RESPONSE_TEMPLATE = "reminders_response";
    @Autowired
    private ReminderManagementService reminderManagementService;

    @Autowired
    private ChannelService channelService;


    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Reminder> activeReminders = reminderManagementService.getActiveRemindersForUser(commandContext.getUserInitiatedContext().getAUserInAServer());
        RemindersModel model = (RemindersModel) ContextConverter.fromCommandContext(commandContext, RemindersModel.class);
        model.setReminders(activeReminders);
        channelService.sendEmbedTemplateInChannel(REMINDERS_RESPONSE_TEMPLATE, model, commandContext.getChannel());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("reminders")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REMIND;
    }
}
