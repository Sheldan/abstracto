package dev.sheldan.abstracto.remind.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.remind.exception.ReminderNotFoundException;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.repository.ReminderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
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
        log.info("Creating reminder for user {} in server {} in message {} to be reminded at {}.",
                userToBeReminded.getAUserInAServer().getUserReference().getId(), userToBeReminded.getGuild().getId(), messageId, timeToBeRemindedAt);

        reminderRepository.save(reminder);
        return reminder;
    }

    @Override
    public Optional<Reminder> loadReminderOptional(Long reminderId) {
        return reminderRepository.findById(reminderId);
    }

    @Override
    public Reminder loadReminder(Long reminderId) {
        return loadReminderOptional(reminderId).orElseThrow(() -> new ReminderNotFoundException(reminderId));
    }

    @Override
    public void setReminded(Reminder reminder) {
        reminder.setReminded(true);
        log.info("Setting reminder {} to reminded.", reminder.getId());
        reminderRepository.save(reminder);
    }

    @Override
    public Reminder saveReminder(Reminder reminder) {
        return reminderRepository.save(reminder);
    }

    @Override
    public List<Reminder> getActiveRemindersForUser(AUserInAServer aUserInAServer) {
        return reminderRepository.getByRemindedUserAndRemindedFalse(aUserInAServer);
    }

    @Override
    public Optional<Reminder> getReminderByAndByUserNotReminded(AUserInAServer aUserInAServer, Long reminderId) {
        return Optional.ofNullable(reminderRepository.getByIdAndRemindedUserAndRemindedFalse(reminderId, aUserInAServer));
    }


}
