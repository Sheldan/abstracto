package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderManagementServiceBean implements ReminderManagementService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Override
    public Reminder createReminder(AServerAChannelAUser userToBeReminded, String text, Instant timeToBeRemindedAt, Long messageId) {
        Reminder reminder = Reminder.builder()
                .channel(userToBeReminded.getChannel())
                .server(userToBeReminded.getGuild())
                .remindedUser(userToBeReminded.getAUserInAServer())
                .reminded(false)
                .text(text)
                .reminderDate(Instant.now())
                .targetDate(timeToBeRemindedAt)
                .messageId(messageId)
        .build();

        reminderRepository.save(reminder);
        return reminder;
    }

    @Override
    public Reminder loadReminder(Long reminderId) {
        return reminderRepository.getOne(reminderId);
    }

    @Override
    public void setReminded(Reminder reminder) {
        reminder.setReminded(true);
        reminderRepository.save(reminder);
    }

    @Override
    public List<Reminder> getActiveRemindersForUser(AUserInAServer aUserInAServer) {
        return reminderRepository.getByRemindedUserAndRemindedFalse(aUserInAServer);
    }

}
