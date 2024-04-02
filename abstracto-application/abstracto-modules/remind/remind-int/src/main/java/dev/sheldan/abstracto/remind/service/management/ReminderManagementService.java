package dev.sheldan.abstracto.remind.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.remind.model.database.Reminder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReminderManagementService {
    Reminder createReminder(AServerAChannelAUser userToBeReminded, String text, Instant timeToBeRemindedAt, Long messageId, Boolean sendInDms, Boolean userCommand);
    Optional<Reminder> loadReminderOptional(Long reminderId);
    Reminder loadReminder(Long reminderId);
    void setReminded(Reminder reminder);
    Reminder saveReminder(Reminder reminder);
    List<Reminder> getActiveRemindersForUser(AUserInAServer aUserInAServer);
    List<Reminder> getActiveUserRemindersForUser(AUser aUser);
    Optional<Reminder> getReminderByAndByUserNotReminded(AUserInAServer aUserInAServer, Long reminderId);
    Optional<Reminder> getReminderByAndByUserNotRemindedForUserCommand(AUser aUser, Long reminderId);
    Optional<Reminder> getReminderByAndByUser(AUserInAServer aUserInAServer, Long reminderId);
}
