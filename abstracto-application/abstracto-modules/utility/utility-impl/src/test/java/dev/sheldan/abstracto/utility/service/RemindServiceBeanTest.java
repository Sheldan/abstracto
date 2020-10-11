package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import static dev.sheldan.abstracto.utility.service.RemindServiceBean.REMINDER_TEMPLATE_TEXT;
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

    private static final Long REMINDER_ID = 5L;

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
        when(channelManagementService.loadChannel(channel.getIdLong())).thenReturn(aChannel);
        Reminder createdReminder = Reminder.builder().targetDate(Instant.now().plus(duration)).text(remindText).id(REMINDER_ID).build();
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
        when(channelManagementService.loadChannel(channel.getIdLong())).thenReturn(aChannel);
        Reminder createdReminder = Reminder.builder().targetDate(Instant.now().plus(duration)).text(remindText).id(REMINDER_ID).build();
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
        Reminder remindedReminder = Reminder.builder().reminded(false).remindedUser(remindedUser).reminderDate(Instant.now()).targetDate(Instant.now()).server(server).channel(aChannel).id(REMINDER_ID).build();
        when(reminderManagementService.loadReminder(REMINDER_ID)).thenReturn(remindedReminder);
        Guild guildMock = Mockito.mock(Guild.class);
        when(botService.getGuildByIdOptional(server.getId())).thenReturn(Optional.of(guildMock));
        when(botService.getTextChannelFromServerOptional(server.getId(), aChannel.getId())).thenReturn(Optional.of(channel));
        Member mockedMember = Mockito.mock(Member.class);
        when(botService.getMemberInServerAsync(server.getId(), remindedUser.getUserReference().getId())).thenReturn(CompletableFuture.completedFuture(mockedMember));
        testUnit.executeReminder(REMINDER_ID);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
    }

    @Test
    public void testExecuteReminderFromNotFoundChannel() {
        AServer server = MockUtils.getServer();
        AChannel aChannel = MockUtils.getTextChannel(server, 4L);
        AUserInAServer remindedUser = Mockito.mock(AUserInAServer.class);
        AUser user = Mockito.mock(AUser.class);
        when(user.getId()).thenReturn(6L);
        when(remindedUser.getUserReference()).thenReturn(user);
        Reminder remindedReminder = Reminder.builder().reminded(false).server(server).remindedUser(remindedUser).channel(aChannel).id(REMINDER_ID).build();
        when(reminderManagementService.loadReminder(REMINDER_ID)).thenReturn(remindedReminder);
        Guild guildMock = Mockito.mock(Guild.class);
        when(botService.getGuildByIdOptional(server.getId())).thenReturn(Optional.of(guildMock));
        when(botService.getTextChannelFromServerOptional(server.getId(), aChannel.getId())).thenReturn(Optional.empty());
        testUnit.executeReminder(REMINDER_ID);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
    }

    @Test
    public void testExecuteReminderFromNotFoundGuild() {
        AServer server = MockUtils.getServer();
        AChannel aChannel = Mockito.mock(AChannel.class);
        when(aChannel.getId()).thenReturn(9L);
        Long reminderId = 5L;
        AUserInAServer remindedUser = Mockito.mock(AUserInAServer.class);
        AUser user = Mockito.mock(AUser.class);
        when(user.getId()).thenReturn(6L);
        when(remindedUser.getUserReference()).thenReturn(user);
        Reminder remindedReminder = Reminder.builder().reminded(false).server(server).channel(aChannel).remindedUser(remindedUser).id(reminderId).build();
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(remindedReminder);
        when(botService.getGuildByIdOptional(server.getId())).thenReturn(Optional.empty());
        testUnit.executeReminder(reminderId);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
    }

    @Test
    public void testExecuteRemindedReminder() {
        Long reminderId = 5L;
        Reminder remindedReminder = Reminder.builder().reminded(true).build();
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(remindedReminder);
        testUnit.executeReminder(reminderId);
        verify(botService, times(0)).getGuildByIdOptional(anyLong());
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

    @Test
    public void testSendReminderText() {
        Member remindedMember = Mockito.mock(Member.class);
        Guild guild = Mockito.mock(Guild.class);
        when(remindedMember.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(8L);
        when(remindedMember.getIdLong()).thenReturn(9L);
        Reminder remindedReminder = Mockito.mock(Reminder.class);
        when(remindedReminder.getTargetDate()).thenReturn(Instant.now());
        when(remindedReminder.getReminderDate()).thenReturn(Instant.now());
        when(reminderManagementService.loadReminder(REMINDER_ID)).thenReturn(remindedReminder);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(REMINDER_TEMPLATE_TEXT), any())).thenReturn(messageToSend);
        when(channelService.sendMessageToSendToChannel(messageToSend, channel)).thenReturn(Arrays.asList(CompletableFuture.completedFuture(null)));
        testUnit.sendReminderText(REMINDER_ID, channel, remindedMember).join();
    }

}
