package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.repository.ReminderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReminderManagementServiceBeanTest {

    @InjectMocks
    private ReminderManagementServiceBean testUnit;

    @Mock
    private ReminderRepository reminderRepository;

    @Test
    public void testCreateReminder() {
        AServer server = MockUtils.getServer();
        AUserInAServer toBeReminded = MockUtils.getUserObject(5L, server);
        AChannel channel = MockUtils.getTextChannel(server, 6L);
        AServerAChannelAUser serverAChannelAUser = AServerAChannelAUser
                .builder()
                .aUserInAServer(toBeReminded)
                .channel(channel)
                .guild(server)
                .build();
        String reminderText = "text";
        Instant reminderTargetDate = Instant.ofEpochSecond(1590615937);
        Long messageId = 5L;
        Reminder reminder = testUnit.createReminder(serverAChannelAUser, reminderText, reminderTargetDate, messageId);
        Assert.assertEquals(messageId, reminder.getMessageId());
        Assert.assertEquals(toBeReminded.getUserReference().getId(), reminder.getRemindedUser().getUserReference().getId());
        Assert.assertEquals(toBeReminded.getUserInServerId(), reminder.getRemindedUser().getUserInServerId());
        Assert.assertEquals(server.getId(), reminder.getServer().getId());
        Assert.assertEquals(server.getId(), reminder.getRemindedUser().getServerReference().getId());
        Assert.assertEquals(reminderText, reminder.getText());
        Assert.assertEquals(reminderTargetDate, reminder.getTargetDate());
        Assert.assertEquals(channel.getId(), reminder.getChannel().getId());
        Assert.assertFalse(reminder.isReminded());
        verify(reminderRepository, times(1)).save(reminder);
    }

    @Test
    public void testSetReminded() {
        Reminder reminder = Reminder.builder().build();
        testUnit.setReminded(reminder);
        reminder.setReminded(true);
        verify(reminderRepository, times(1)).save(reminder);
    }

    @Test
    public void testSaveReminder() {
        Reminder reminder = Reminder.builder().build();
        testUnit.saveReminder(reminder);
        verify(reminderRepository, times(1)).save(reminder);
    }

    @Test
    public void testRetrieveActiveReminders() {
        AServer server = MockUtils.getServer();
        AUserInAServer user = MockUtils.getUserObject(5L, server);
        Reminder reminder1 = Reminder.builder().build();
        Reminder reminder2 = Reminder.builder().build();
        List<Reminder> reminders = Arrays.asList(reminder1, reminder2);
        when(reminderRepository.getByRemindedUserAndRemindedFalse(user)).thenReturn(reminders);
        List<Reminder> activeRemindersForUser = testUnit.getActiveRemindersForUser(user);
        for (int i = 0; i < reminders.size(); i++) {
            Reminder reference = reminders.get(i);
            Reminder returned = activeRemindersForUser.get(0);
            Assert.assertEquals(reference, returned);
        }
        Assert.assertEquals(reminders.size(), activeRemindersForUser.size());
    }

    @Test
    public void testGetReminderByIdAndNotReminded() {
        Long reminderId = 6L;
        Reminder reminder = Reminder.builder().id(6L).build();
        AServer server = MockUtils.getServer();
        AUserInAServer user = MockUtils.getUserObject(5L, server);
        when(reminderRepository.getByIdAndRemindedUserAndRemindedFalse(reminderId, user)).thenReturn(reminder);
        Optional<Reminder> returned = testUnit.getReminderByAndByUserNotReminded(user, reminderId);
        Assert.assertTrue(returned.isPresent());
        returned.ifPresent(returnedReminder -> Assert.assertEquals(reminder, returnedReminder));
    }

    @Test
    public void testGetReminderByIdAndNotRemindedNothingFound() {
        Long reminderId = 6L;
        AServer server = MockUtils.getServer();
        AUserInAServer user = MockUtils.getUserObject(5L, server);
        when(reminderRepository.getByIdAndRemindedUserAndRemindedFalse(reminderId, user)).thenReturn(null);
        Optional<Reminder> returned = testUnit.getReminderByAndByUserNotReminded(user, reminderId);
        Assert.assertFalse(returned.isPresent());
    }

    @Test
    public void testLoadingReminder() {
        Long reminderId = 5L;
        Reminder reminderToLoad = Reminder.builder().build();
        when(reminderRepository.findById(reminderId)).thenReturn(Optional.of(reminderToLoad));
        Optional<Reminder> returned = testUnit.loadReminderOptional(reminderId);
        Assert.assertTrue(returned.isPresent());
        returned.ifPresent(returnedReminder -> Assert.assertEquals(reminderToLoad, returnedReminder));
    }

    @Test
    public void testLoadingReminderNotFound() {
        Long reminderId = 5L;
        when(reminderRepository.findById(reminderId)).thenReturn(Optional.empty());
        Optional<Reminder> returned = testUnit.loadReminderOptional(reminderId);
        Assert.assertFalse(returned.isPresent());
    }

}
