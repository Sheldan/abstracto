package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.core.models.converter.ServerConverter;
import dev.sheldan.abstracto.core.models.converter.UserConverter;
import dev.sheldan.abstracto.core.models.converter.UserInServerConverter;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.repository.ReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderManagementServiceBean {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private ChannelConverter channelConverter;

    @Autowired
    private ServerConverter serverConverter;

    @Autowired
    private UserInServerConverter userConverter;

    public Reminder createReminder(AServerAChannelAUser userToBeReminded, String text, Instant timeToBeRemindedAt, Long messageId) {
        Reminder reminder = Reminder.builder()
                .channel(channelConverter.fromDto(userToBeReminded.getChannel()))
                .server(serverConverter.fromDto(userToBeReminded.getGuild()))
                .remindedUser(userConverter.fromDto(userToBeReminded.getAUserInAServer()))
                .reminded(false)
                .text(text)
                .reminderDate(Instant.now())
                .targetDate(timeToBeRemindedAt)
                .messageId(messageId)
        .build();

        reminderRepository.save(reminder);
        return reminder;
    }

    public Reminder loadReminder(Long reminderId) {
        return reminderRepository.getOne(reminderId);
    }

    public void setReminded(Reminder reminder) {
        reminder.setReminded(true);
        reminderRepository.save(reminder);
    }

    public List<Reminder> getActiveRemindersForUser(UserInServerDto aUserInAServer) {
        return reminderRepository.getByRemindedUserAndRemindedFalse(userConverter.fromDto(aUserInAServer));
    }

}
