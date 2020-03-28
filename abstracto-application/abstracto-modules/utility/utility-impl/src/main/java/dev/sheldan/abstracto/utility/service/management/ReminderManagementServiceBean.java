package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.utility.models.Reminder;
import dev.sheldan.abstracto.utility.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ReminderManagementServiceBean implements ReminderManagementService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Override
    public Reminder createReminder(AServerAChannelAUser userToBeReminded, String text, Instant timeToBeRemindedAt, Long messageId) {
        Reminder reminder = Reminder.builder()
                .channel(userToBeReminded.getChannel())
                .server(userToBeReminded.getGuild())
                .toBeReminded(userToBeReminded.getAUserInAServer())
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

}
