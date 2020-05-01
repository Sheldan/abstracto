package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.ReminderModel;

import java.time.Duration;

public interface ReminderService {
    void createReminderInForUser(AUserInAServer user, String remindText, Duration remindIn, ReminderModel reminderModel);
    void executeReminder(Long reminderId);
    void unRemind(Long reminderId, AUserInAServer userInAServer);
}
