package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.exception.ReminderNotFoundException;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import dev.sheldan.abstracto.utility.models.template.commands.reminder.ExecutedReminderModel;
import dev.sheldan.abstracto.utility.service.management.ReminderManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobDataMap;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemindServiceBeanTest {

    @InjectMocks
    private RemindServiceBean testUnit;

    @Mock
    private ReminderManagementService reminderManagementService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private TemplateService templateService;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private BotService botService;

    @Mock
    private ReminderService self;

    @Mock
    private ChannelService channelService;

    @Mock
    private Message message;

    @Mock
    private TextChannel channel;

    @Mock
    private ScheduledExecutorService instantReminderScheduler;

    @Before
    public void setup() {
        when(message.getIdLong()).thenReturn(5L);
        when(channel.getIdLong()).thenReturn(5L);
        when(message.getChannel()).thenReturn(channel);
    }

    @Test
    public void createReminderWithScheduler() {
        AServer server = MockUtils.getServer();
        AUserInAServer user = MockUtils.getUserObject(4L, server);
        AChannel aChannel = MockUtils.getTextChannel(server, 5L);
        String remindText = "text";
        Duration duration = Duration.ofSeconds(62);
        when(channelManagementService.loadChannel(channel.getIdLong())).thenReturn(Optional.of(aChannel));
        Long reminderId = 5L;
        Reminder createdReminder = Reminder.builder().targetDate(Instant.now().plus(duration)).text(remindText).id(reminderId).build();
        Long messageId = 5L;
        when(reminderManagementService.createReminder(any(AServerAChannelAUser.class), eq(remindText), any(Instant.class), eq(messageId))).thenReturn(createdReminder);
        String triggerKey = "trigger";
        when(schedulerService.executeJobWithParametersOnce(eq("reminderJob"), eq("utility"), any(JobDataMap.class), eq(Date.from(createdReminder.getTargetDate())))).thenReturn(triggerKey);
        Reminder returnedReminder = testUnit.createReminderInForUser(user, remindText, duration, message);
        createdReminder.setJobTriggerKey(triggerKey);
        verify(reminderManagementService, times(1)).saveReminder(createdReminder);
        Assert.assertEquals(remindText, returnedReminder.getText());
        Assert.assertEquals(createdReminder.getId(), returnedReminder.getId());
        Assert.assertEquals(triggerKey, returnedReminder.getJobTriggerKey());
    }

    @Test
    public void createReminderWithoutScheduler() {
        AServer server = MockUtils.getServer();
        AUserInAServer user = MockUtils.getUserObject(4L, server);
        AChannel aChannel = MockUtils.getTextChannel(server, 5L);
        String remindText = "text";
        Duration duration = Duration.ofSeconds(50);
        when(channelManagementService.loadChannel(channel.getIdLong())).thenReturn(Optional.of(aChannel));
        Long reminderId = 5L;
        Reminder createdReminder = Reminder.builder().targetDate(Instant.now().plus(duration)).text(remindText).id(reminderId).build();
        Long messageId = 5L;
        when(reminderManagementService.createReminder(any(AServerAChannelAUser.class), eq(remindText), any(Instant.class), eq(messageId))).thenReturn(createdReminder);
        Reminder returnedReminder = testUnit.createReminderInForUser(user, remindText, duration, message);
        verify(reminderManagementService, times(0)).saveReminder(createdReminder);
        Assert.assertEquals(remindText, returnedReminder.getText());
        Assert.assertEquals(createdReminder.getId(), returnedReminder.getId());
    }

    @Test
    public void testExecuteReminder() {
        AServer server = MockUtils.getServer();
        AChannel aChannel = MockUtils.getTextChannel(server, 4L);
        AUserInAServer remindedUser = MockUtils.getUserObject(5L, server);
        Long reminderId = 5L;
        Reminder remindedReminder = Reminder.builder().reminded(false).remindedUser(remindedUser).reminderDate(Instant.now()).targetDate(Instant.now()).server(server).channel(aChannel).id(reminderId).build();
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(Optional.of(remindedReminder));
        Guild guildMock = Mockito.mock(Guild.class);
        when(botService.getGuildById(server.getId())).thenReturn(Optional.of(guildMock));
        when(botService.getTextChannelFromServer(server.getId(), aChannel.getId())).thenReturn(Optional.of(channel));
        Member mockedMember = Mockito.mock(Member.class);
        when(botService.getMemberInServer(server.getId(), remindedUser.getUserReference().getId())).thenReturn(mockedMember);
        MessageToSend messageToSend = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq("remind_reminder"), any(ExecutedReminderModel.class))).thenReturn(messageToSend);
        testUnit.executeReminder(reminderId);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
        verify(channelService, times(1)).sendMessageToSendToChannel(messageToSend, channel);
    }

    @Test
    public void testExecuteReminderFromNotFoundChannel() {
        AServer server = MockUtils.getServer();
        AChannel aChannel = MockUtils.getTextChannel(server, 4L);
        Long reminderId = 5L;
        Reminder remindedReminder = Reminder.builder().reminded(false).server(server).channel(aChannel).id(reminderId).build();
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(Optional.of(remindedReminder));
        Guild guildMock = Mockito.mock(Guild.class);
        when(botService.getGuildById(server.getId())).thenReturn(Optional.of(guildMock));
        when(botService.getTextChannelFromServer(server.getId(), aChannel.getId())).thenReturn(Optional.empty());
        testUnit.executeReminder(reminderId);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
    }

    @Test
    public void testExecuteReminderFromNotFoundGuild() {
        AServer server = MockUtils.getServer();
        Long reminderId = 5L;
        Reminder remindedReminder = Reminder.builder().reminded(false).server(server).id(reminderId).build();
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(Optional.of(remindedReminder));
        when(botService.getGuildById(server.getId())).thenReturn(Optional.empty());
        testUnit.executeReminder(reminderId);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
    }

    @Test
    public void testExecuteRemindedReminder() {
        Long reminderId = 5L;
        Reminder remindedReminder = Reminder.builder().reminded(true).build();
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(Optional.of(remindedReminder));
        testUnit.executeReminder(reminderId);
        verify(botService, times(0)).getGuildById(anyLong());
    }

    @Test(expected = ReminderNotFoundException.class)
    public void testExecuteIllegalReminderId() {
        Long reminderId = 5L;
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(Optional.empty());
        testUnit.executeReminder(reminderId);
        verify(botService, times(0)).getGuildById(anyLong());
    }

    @Test
    public void testUnRemindScheduledReminder() {
        AServer server = MockUtils.getServer();
        AUserInAServer remindedUser = MockUtils.getUserObject(5L, server);
        Long reminderId = 5L;
        String triggerKey = "trigger";
        Reminder reminderToUnRemind = Reminder.builder().jobTriggerKey(triggerKey).id(reminderId).build();
        when(reminderManagementService.getReminderByAndByUserNotReminded(remindedUser, reminderId)).thenReturn(Optional.of(reminderToUnRemind));
        testUnit.unRemind(reminderId, remindedUser);
        verify(schedulerService, times(1)).stopTrigger(triggerKey);
    }

    @Test
    public void testUnRemindNonScheduledReminder() {
        AServer server = MockUtils.getServer();
        AUserInAServer remindedUser = MockUtils.getUserObject(5L, server);
        Long reminderId = 5L;
        Reminder reminderToUnRemind = Reminder.builder().id(reminderId).build();
        when(reminderManagementService.getReminderByAndByUserNotReminded(remindedUser, reminderId)).thenReturn(Optional.of(reminderToUnRemind));
        testUnit.unRemind(reminderId, remindedUser);
        verify(schedulerService, times(0)).stopTrigger(anyString());
    }

    @Test(expected = ReminderNotFoundException.class)
    public void testUnRemindNonExistingReminder() {
        AServer server = MockUtils.getServer();
        AUserInAServer remindedUser = MockUtils.getUserObject(5L, server);
        Long reminderId = 5L;
        when(reminderManagementService.getReminderByAndByUserNotReminded(remindedUser, reminderId)).thenReturn(Optional.empty());
        testUnit.unRemind(reminderId, remindedUser);
    }

}
