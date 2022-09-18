package dev.sheldan.abstracto.remind.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.remind.exception.ReminderNotFoundException;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.service.management.ReminderManagementService;
import dev.sheldan.abstracto.remind.service.management.ReminderParticipantManagementService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    private GuildMessageChannelUnion guildMessageChannelUnion;

    @Mock
    private MessageChannelUnion messageChannelUnion;

    @Mock
    private ScheduledExecutorService instantReminderScheduler;

    @Mock
    private ReminderParticipantManagementService reminderParticipantManagementService;

    @Mock
    private AServer server;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private AUser user;

    @Mock
    private User jdaUser;

    @Mock
    private AChannel aChannel;

    private static final Long REMINDER_ID = 5L;
    private static final Long CHANNEL_ID = 6L;
    private static final Long SERVER_ID = 8L;
    private static final Long USER_ID = 9L;

    @Before
    public void setup() {
        when(message.getIdLong()).thenReturn(5L);
        when(guildMessageChannelUnion.getIdLong()).thenReturn(5L);
        when(message.getChannel()).thenReturn(messageChannelUnion);
    }

    @Test
    public void createReminderWithScheduler() {
        String remindText = "text";
        String triggerKey = "trigger";
        Duration duration = Duration.ofSeconds(62);
        when(message.getChannel()).thenReturn(messageChannelUnion);
        when(guildMessageChannelUnion.getIdLong()).thenReturn(CHANNEL_ID);
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
        when(message.getChannel()).thenReturn(messageChannelUnion);
        when(messageChannelUnion.getIdLong()).thenReturn(CHANNEL_ID);
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
        when(channelService.getMessageChannelFromServerOptional(SERVER_ID, CHANNEL_ID)).thenReturn(Optional.of(guildMessageChannelUnion));
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
        when(channelService.getMessageChannelFromServerOptional(SERVER_ID, CHANNEL_ID)).thenReturn(Optional.empty());
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
        when(remindedMember.getUser()).thenReturn(jdaUser);
        when(jdaUser.getDefaultAvatar()).thenReturn(Mockito.mock(ImageProxy.class));
        Reminder remindedReminder = Mockito.mock(Reminder.class);
        when(remindedReminder.getTargetDate()).thenReturn(Instant.now());
        when(remindedReminder.getReminderDate()).thenReturn(Instant.now());
        when(reminderManagementService.loadReminder(REMINDER_ID)).thenReturn(remindedReminder);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(guildMessageChannelUnion.getGuild()).thenReturn(guild);
        when(templateService.renderEmbedTemplate(eq(RemindServiceBean.REMINDER_TEMPLATE_TEXT), any(), eq(SERVER_ID))).thenReturn(messageToSend);
        when(channelService.sendMessageToSendToChannel(messageToSend, messageChannelUnion)).thenReturn(Arrays.asList(CompletableFuture.completedFuture(null)));
        CompletableFuture<Void> future = testUnit.sendReminderText(REMINDER_ID, guildMessageChannelUnion, remindedMember);
        future.join();
        Assert.assertFalse(future.isCompletedExceptionally());
    }

}
