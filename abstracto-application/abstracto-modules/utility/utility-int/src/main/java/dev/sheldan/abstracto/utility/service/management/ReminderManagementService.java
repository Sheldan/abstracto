package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.utility.models.Reminder;

import java.time.Instant;

public interface ReminderManagementService {
    Reminder createReminder(AServerAChannelAUser userToBeReminded, String text, Instant timeToBeRemindedAt, Long messageId);
    Reminder loadReminder(Long reminderId);
}
