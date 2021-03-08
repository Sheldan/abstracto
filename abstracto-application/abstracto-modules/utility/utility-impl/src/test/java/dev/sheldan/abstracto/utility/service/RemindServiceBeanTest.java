package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
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
    private GuildService guildService;

    @Mock
    private MemberService memberService;

    @Mock
    private RemindServiceBean self;

    @Mock
    private ChannelService channelService;

    @Mock
    private Message message;

    @Mock
    private TextChannel channel;

    @Mock
    private ScheduledExecutorService instantReminderScheduler;

    @Mock
    private AServer server;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private AUser user;

    @Mock
    private AChannel aChannel;

    private static final Long REMINDER_ID = 5L;
    private static final Long CHANNEL_ID = 6L;
    private static final Long SERVER_ID = 8L;
    private static final Long USER_ID = 9L;

    @Before
    public void setup() {
        when(message.getIdLong()).thenReturn(5L);
        when(channel.getIdLong()).thenReturn(5L);
        when(message.getChannel()).thenReturn(channel);
    }

    @Test
    public void createReminderWithScheduler() {
        String remindText = "text";
        String triggerKey = "trigger";
        Duration duration = Duration.ofSeconds(62);
        when(message.getChannel()).thenReturn(channel);
        when(channel.getIdLong()).thenReturn(CHANNEL_ID);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(aChannel);
        Instant targetDate = Instant.now().plus(duration);
        Reminder createdReminder = Mockito.mock(Reminder.class);
        when(createdReminder.getTargetDate()).thenReturn(targetDate);
        Long messageId = 5L;
        when(reminderManagementService.createReminder(any(AServerAChannelAUser.class), eq(remindText), any(Instant.class), eq(messageId))).thenReturn(createdReminder);
        when(schedulerService.executeJobWithParametersOnce(eq("reminderJob"), eq("utility"), any(JobParameters.class), eq(Date.from(targetDate)))).thenReturn(triggerKey);
        Reminder returnedReminder = testUnit.createReminderInForUser(aUserInAServer, remindText, duration, message);
        verify(reminderManagementService, times(1)).saveReminder(createdReminder);
        Assert.assertEquals(createdReminder, returnedReminder);
    }

    @Test
    public void createReminderWithoutScheduler() {
        String remindText = "text";
        Duration duration = Duration.ofSeconds(50);
        when(message.getChannel()).thenReturn(channel);
        when(channel.getIdLong()).thenReturn(CHANNEL_ID);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(aChannel);
        Reminder createdReminder = Mockito.mock(Reminder.class);
        when(createdReminder.getText()).thenReturn(remindText);
        Long messageId = 5L;
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(reminderManagementService.createReminder(any(AServerAChannelAUser.class), eq(remindText), any(Instant.class), eq(messageId))).thenReturn(createdReminder);
        Reminder returnedReminder = testUnit.createReminderInForUser(aUserInAServer, remindText, duration, message);
        verify(reminderManagementService, times(0)).saveReminder(createdReminder);
        Assert.assertEquals(remindText, returnedReminder.getText());
        Assert.assertEquals(createdReminder.getId(), returnedReminder.getId());
    }

    @Test
    public void testExecuteReminder() {
        Reminder remindedReminder = Mockito.mock(Reminder.class);
        when(remindedReminder.getRemindedUser()).thenReturn(aUserInAServer);
        when(remindedReminder.getChannel()).thenReturn(aChannel);
        when(remindedReminder.getServer()).thenReturn(server);
        when(aChannel.getId()).thenReturn(CHANNEL_ID);
        when(server.getId()).thenReturn(SERVER_ID);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);
        when(reminderManagementService.loadReminder(REMINDER_ID)).thenReturn(remindedReminder);
        Guild guildMock = Mockito.mock(Guild.class);
        when(guildService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.of(guildMock));
        when(channelService.getTextChannelFromServerOptional(SERVER_ID, CHANNEL_ID)).thenReturn(Optional.of(channel));
        Member mockedMember = Mockito.mock(Member.class);
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(mockedMember));
        testUnit.executeReminder(REMINDER_ID);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
    }

    @Test
    public void testExecuteReminderFromNotFoundChannel() {
        Reminder remindedReminder = Mockito.mock(Reminder.class);
        when(remindedReminder.getChannel()).thenReturn(aChannel);
        when(remindedReminder.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(aChannel.getId()).thenReturn(CHANNEL_ID);
        when(reminderManagementService.loadReminder(REMINDER_ID)).thenReturn(remindedReminder);
        Guild guildMock = Mockito.mock(Guild.class);
        when(guildService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.of(guildMock));
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(remindedReminder.getRemindedUser()).thenReturn(aUserInAServer);
        when(channelService.getTextChannelFromServerOptional(SERVER_ID, CHANNEL_ID)).thenReturn(Optional.empty());
        testUnit.executeReminder(REMINDER_ID);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
        verify(self, times(0)).sendReminderText(anyLong(), any(), any(Member.class));
    }

    @Test
    public void testExecuteReminderFromNotFoundGuild() {
        Long reminderId = 5L;
        Reminder remindedReminder = Mockito.mock(Reminder.class);
        when(remindedReminder.getChannel()).thenReturn(aChannel);
        when(remindedReminder.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(remindedReminder.getRemindedUser()).thenReturn(aUserInAServer);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(reminderManagementService.loadReminder(reminderId)).thenReturn(remindedReminder);
        when(guildService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.empty());
        testUnit.executeReminder(reminderId);
        verify(reminderManagementService, times(1)).setReminded(remindedReminder);
    }

    @Test
    public void testExecuteRemindedReminder() {
        Reminder remindedReminder = Mockito.mock(Reminder.class);
        when(remindedReminder.isReminded()).thenReturn(true);
        when(reminderManagementService.loadReminder(REMINDER_ID)).thenReturn(remindedReminder);
        testUnit.executeReminder(REMINDER_ID);
        verify(guildService, times(0)).getGuildByIdOptional(anyLong());
    }

    @Test
    public void testUnRemindScheduledReminder() {
        String triggerKey = "trigger";
        Reminder reminderToUnRemind = Mockito.mock(Reminder.class);
        when(reminderToUnRemind.getJobTriggerKey()).thenReturn(triggerKey);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(reminderManagementService.getReminderByAndByUserNotReminded(aUserInAServer, REMINDER_ID)).thenReturn(Optional.of(reminderToUnRemind));
        testUnit.unRemind(REMINDER_ID, aUserInAServer);
        verify(schedulerService, times(1)).stopTrigger(triggerKey);
    }

    @Test
    public void testUnRemindNonScheduledReminder() {
        Reminder reminderToUnRemind = Mockito.mock(Reminder.class);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(reminderManagementService.getReminderByAndByUserNotReminded(aUserInAServer, REMINDER_ID)).thenReturn(Optional.of(reminderToUnRemind));
        testUnit.unRemind(REMINDER_ID, aUserInAServer);
        verify(schedulerService, times(0)).stopTrigger(anyString());
    }

    @Test(expected = ReminderNotFoundException.class)
    public void testUnRemindNonExistingReminder() {
        when(aUserInAServer.getServerReference()).thenReturn(server);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(reminderManagementService.getReminderByAndByUserNotReminded(aUserInAServer, REMINDER_ID)).thenReturn(Optional.empty());
        testUnit.unRemind(REMINDER_ID, aUserInAServer);
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
        CompletableFuture<Void> future = testUnit.sendReminderText(REMINDER_ID, channel, remindedMember);
        future.join();
        Assert.assertFalse(future.isCompletedExceptionally());
    }

}
