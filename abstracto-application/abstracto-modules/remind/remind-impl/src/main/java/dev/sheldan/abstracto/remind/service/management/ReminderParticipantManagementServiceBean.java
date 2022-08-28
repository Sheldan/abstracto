package dev.sheldan.abstracto.remind.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.database.ReminderParticipant;
import dev.sheldan.abstracto.remind.model.database.embed.ReminderUserId;
import dev.sheldan.abstracto.remind.repository.ReminderParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReminderParticipantManagementServiceBean implements ReminderParticipantManagementService {

    @Autowired
    private ReminderParticipantRepository reminderParticipantRepository;

    @Override
    public ReminderParticipant addMemberToReminder(Reminder reminder, AUserInAServer aUserInAServer) {
        ReminderUserId id = new ReminderUserId(reminder.getId(), aUserInAServer.getUserInServerId());

        ReminderParticipant reminderParticipant = ReminderParticipant
                .builder()
                .reminder(reminder)
                .participator(aUserInAServer)
                .server(aUserInAServer.getServerReference())
                .reminderParticipantId(id)
                .build();
        return reminderParticipantRepository.save(reminderParticipant);
    }

    @Override
    public void removeMemberFromReminder(Reminder reminder, AUserInAServer aUserInAServer) {
        ReminderUserId id = new ReminderUserId(reminder.getId(), aUserInAServer.getUserInServerId());
        reminderParticipantRepository.deleteById(id);
    }

    @Override
    public void removeMemberFromReminder(ReminderParticipant reminderParticipant) {
        reminderParticipantRepository.delete(reminderParticipant);
    }

    @Override
    public Optional<ReminderParticipant> getReminderParticipant(Reminder reminder, AUserInAServer aUserInAServer) {
        ReminderUserId id = new ReminderUserId(reminder.getId(), aUserInAServer.getUserInServerId());
        return reminderParticipantRepository.findById(id);
    }

    @Override
    public boolean isReminderParticipator(Reminder reminder, AUserInAServer aUserInAServer) {
        ReminderUserId id = new ReminderUserId(reminder.getId(), aUserInAServer.getUserInServerId());
        return reminderParticipantRepository.existsById(id);
    }

    @Override
    public List<ReminderParticipant> getReminderParticipants(Reminder reminder) {
        return reminderParticipantRepository.findAllByReminder(reminder);
    }
}
