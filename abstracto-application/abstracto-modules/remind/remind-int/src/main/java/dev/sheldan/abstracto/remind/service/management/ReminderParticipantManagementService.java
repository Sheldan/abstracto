package dev.sheldan.abstracto.remind.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.database.ReminderParticipant;

import java.util.List;
import java.util.Optional;

public interface ReminderParticipantManagementService {

    ReminderParticipant addMemberToReminder(Reminder reminder, AUserInAServer aUserInAServer);
    void removeMemberFromReminder(Reminder reminder, AUserInAServer aUserInAServer);
    void removeMemberFromReminder(ReminderParticipant reminderParticipant);
    Optional<ReminderParticipant> getReminderParticipant(Reminder reminder, AUserInAServer aUserInAServer);
    boolean isReminderParticipator(Reminder reminder, AUserInAServer aUserInAServer);
    List<ReminderParticipant> getReminderParticipants(Reminder reminder);
}
