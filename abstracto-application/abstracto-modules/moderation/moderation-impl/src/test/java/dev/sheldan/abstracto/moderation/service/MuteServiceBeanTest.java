package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.posttargets.MutingPostTarget;
import dev.sheldan.abstracto.moderation.exception.MuteException;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.models.database.MuteRole;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteLog;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteNotification;
import dev.sheldan.abstracto.moderation.models.template.commands.UnMuteLog;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.moderation.service.management.MuteRoleManagementService;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.api.entities.*;
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
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MuteServiceBeanTest {

    @InjectMocks
    private MuteServiceBean testUnit;

    @Mock
    private MuteRoleManagementService muteRoleManagementService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private MuteManagementService muteManagementService;

    @Mock
    private TemplateService templateService;

    @Mock
    private BotService botService;

    @Mock
    private MessageService messageService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private MuteService self;

    @Mock
    private ScheduledExecutorService executorService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private AUserInAServer userBeingMuted;

    @Mock
    private AUserInAServer userMuting;

    @Mock
    private User jdaUserBeingMuted;

    @Mock
    private Member memberBeingMuted;

    @Mock
    private Member memberMuting;

    @Mock
    private AServer server;

    @Mock
    private AChannel aChannel;

    @Mock
    private MessageChannel channel;

    @Mock
    private Message cause;

    @Mock
    private Guild guild;

    @Mock
    private ARole aRole;

    @Mock
    private MuteRole muteRole;

    @Mock
    private MessageToSend messageToSend;

    private static final Long CHANNEL_ID = 8L;

    private static final String REASON = "reason";
    private static final String NOTIFICATION_TEXT = "text";
    private static final String TRIGGER = "trigger";

    @Before
    public void setup() {
        server = MockUtils.getServer();
        userBeingMuted = MockUtils.getUserObject(3L, server);
        userMuting = MockUtils.getUserObject(4L, server);
        aChannel = MockUtils.getTextChannel(server, CHANNEL_ID);
        jdaUserBeingMuted = Mockito.mock(User.class);
        guild = Mockito.mock(Guild.class);
        memberMuting = Mockito.mock(Member.class);
        memberBeingMuted = Mockito.mock(Member.class);
        channel = Mockito.mock(MessageChannel.class);
        cause = Mockito.mock(Message.class);
        aRole = ARole.builder().build();
        muteRole = MuteRole.builder().role(aRole).build();
        messageToSend = Mockito.mock(MessageToSend.class);
    }

    @Test
    public void testMuteUserWithScheduler() {
        Instant unMuteDate = longerMute();
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        FullUser mutedUser = FullUser.builder().member(memberBeingMuted).aUserInAServer(userBeingMuted).build();
        FullUser mutingUser = FullUser.builder().member(memberMuting).aUserInAServer(userMuting).build();
        setupShortMute(unMuteDate);
        when(schedulerService.executeJobWithParametersOnce(eq("unMuteJob"), eq("moderation"), any(JobDataMap.class), eq(Date.from(unMuteDate)))).thenReturn(TRIGGER);
        testUnit.muteUser(mutedUser, mutingUser, REASON, unMuteDate, cause);
        verify(messageService, times(1)).sendMessageToUser(jdaUserBeingMuted, NOTIFICATION_TEXT, channel);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(roleService, times(1)).addRoleToUser(userBeingMuted, aRole);
    }

    @Test
    public void testMuteWithDirectUnMute() {
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        FullUser mutedUser = FullUser.builder().member(memberBeingMuted).aUserInAServer(userBeingMuted).build();
        FullUser mutingUser = FullUser.builder().member(memberMuting).aUserInAServer(userMuting).build();
        Instant unMuteDate = shorterMute();
        setupShortMute(unMuteDate);
        testUnit.muteUser(mutedUser, mutingUser, REASON, unMuteDate, cause);
        verifyDirectMute();
    }

    @Test(expected = MuteException.class)
    public void testMuteUserWithoutMuteRole() {
        FullUser mutedUser = FullUser.builder().aUserInAServer(userBeingMuted).build();
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(false);
        testUnit.muteUser(mutedUser, FullUser.builder().build(), REASON, longerMute(), Mockito.mock(Message.class));
    }

    @Test
    public void testCancelUnMuteJob() {
        Mute mute = Mute.builder().triggerKey(TRIGGER).build();
        testUnit.cancelUnMuteJob(mute);
        verify(schedulerService, times(1)).stopTrigger(TRIGGER);
    }

    @Test
    public void testCancelNotExistingJob() {
        Mute mute = Mute.builder().build();
        testUnit.cancelUnMuteJob(mute);
        verify(schedulerService, times(0)).stopTrigger(anyString());
    }

    @Test
    public void testMuteMember() {
        when(userInServerManagementService.loadUser(memberBeingMuted)).thenReturn(userBeingMuted);
        when(userInServerManagementService.loadUser(memberMuting)).thenReturn(userMuting);
        Instant unMuteDate = shorterMute();
        setupShortMute(unMuteDate);
        testUnit.muteMember(memberBeingMuted, memberMuting, REASON, unMuteDate, cause);
        verifyDirectMute();
    }

    @Test
    public void testMuteAUserInAServer() {
        when(botService.getMemberInServer(userBeingMuted)).thenReturn(memberBeingMuted);
        when(botService.getMemberInServer(userMuting)).thenReturn(memberMuting);
        Instant unMuteDate = shorterMute();
        setupShortMute(unMuteDate);
        testUnit.muteAUserInAServer(userBeingMuted, userMuting, REASON, unMuteDate, cause);
        verifyDirectMute();
    }

    @Test
    public void testMuteMemberWithLog() {
        when(userInServerManagementService.loadUser(memberBeingMuted)).thenReturn(userBeingMuted);
        when(userInServerManagementService.loadUser(memberMuting)).thenReturn(userMuting);
        Instant unMuteDate = shorterMute();
        setupShortMute(unMuteDate);
        MuteLog muteLog = MuteLog.builder().server(server).build();
        when(templateService.renderEmbedTemplate(eq(MuteServiceBean.MUTE_LOG_TEMPLATE), any(MuteLog.class))).thenReturn(messageToSend);
        testUnit.muteMemberWithLog(memberBeingMuted, memberMuting, REASON, unMuteDate, muteLog, cause);
        verifyDirectMute();
        verify(templateService, times(1)).renderEmbedTemplate(eq(MuteServiceBean.MUTE_LOG_TEMPLATE), any(MuteLog.class));
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, MutingPostTarget.MUTE_LOG, server.getId());
    }

    @Test
    public void testUnMuteMemberWhoseMuteEnded() {
        Mute mute = Mockito.mock(Mute.class);
        when(mute.getMuteEnded()).thenReturn(true);
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        testUnit.unMuteUser(mute);
        verifyNoUnMuteHappened();
    }

    @Test
    public void testEndMute() {
        Long muteId = 6L;
        setupUnMuteMocks(true);
        Mute mute = Mockito.mock(Mute.class);
        when(mute.getMuteEnded()).thenReturn(false);
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(mute.getMutingServer()).thenReturn(server);
        when(muteManagementService.findMute(muteId)).thenReturn(Optional.of(mute));
        testUnit.endMute(muteId);
        verifyUnMute(1);
    }

    @Test
    public void testEndNonExistingMute() {
        Long muteId = 6L;
        when(muteManagementService.findMute(muteId)).thenReturn(Optional.empty());
        testUnit.endMute(muteId);
        verifyNoUnMuteHappened();
    }

    @Test
    public void testUnMuteMemberInGuild() {
        executeUnMuteWithLogTest(true, 1);
    }

    @Test
    public void testUnMuteMemberWhoLeftGuild() {
        executeUnMuteWithLogTest(false, 0);
    }

    @Test
    public void testCompletelyUnMuteNotMutedUser() {
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList());
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(0)).saveMute(any(Mute.class));
    }

    @Test
    public void testCompletelyUnMuteNotScheduledMuteUser() {
        Mute mute = Mockito.mock(Mute.class);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(0)).stopTrigger(anyString());
    }

    @Test
    public void testCompletelyUnMuteScheduledMuteUser() {
        Mute mute = Mockito.mock(Mute.class);
        when(mute.getTriggerKey()).thenReturn(TRIGGER);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(1)).stopTrigger(TRIGGER);
    }

    @Test
    public void testCompletelyUnMuteMember() {
        Mute mute = Mockito.mock(Mute.class);
        when(mute.getTriggerKey()).thenReturn(TRIGGER);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        when(userInServerManagementService.loadUser(memberBeingMuted)).thenReturn(userBeingMuted);
        testUnit.completelyUnMuteMember(memberBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(1)).stopTrigger(TRIGGER);
    }

    private void verifyNoUnMuteHappened() {
        verify(muteManagementService, times(0)).saveMute(any(Mute.class));
        verify(roleService, times(0)).removeRoleFromUser(eq(userBeingMuted), any(ARole.class));
        verify(postTargetService, times(0)).sendEmbedInPostTarget(any(MessageToSend.class), eq(MutingPostTarget.MUTE_LOG), eq(server.getId()));
    }

    private void executeUnMuteWithLogTest(boolean stillInGuild, int roleRemovals) {
        Mute mute = Mockito.mock(Mute.class);
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(mute.getMutingServer()).thenReturn(server);
        setupUnMuteMocks(stillInGuild);

        testUnit.unMuteUser(mute);

        verifyUnMute(roleRemovals);
    }

    private void setupUnMuteMocks(boolean stillInGuild) {
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(botService.getGuildByIdNullable(server.getId())).thenReturn(guild);
        when(botService.isUserInGuild(guild, userBeingMuted)).thenReturn(stillInGuild);
        when(botService.getMemberInServer(userBeingMuted)).thenReturn(memberBeingMuted);
        when(templateService.renderEmbedTemplate(eq(MuteServiceBean.UN_MUTE_LOG_TEMPLATE), any(UnMuteLog.class))).thenReturn(messageToSend);
    }

    private void verifyUnMute(int roleRemovals) {
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(roleService, times(roleRemovals)).removeRoleFromUser(userBeingMuted, aRole);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, MutingPostTarget.MUTE_LOG, server.getId());
    }

    private void setupShortMute(Instant unMuteDate) {
        Long muteId = 12L;
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(channel.getIdLong()).thenReturn(CHANNEL_ID);
        when(cause.getGuild()).thenReturn(guild);
        when(cause.getChannel()).thenReturn(channel);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(Optional.of(aChannel));
        Mute createdMute = Mute.builder().id(muteId).build();
        when(muteManagementService.createMute(eq(userBeingMuted), eq(userMuting), eq(REASON), eq(unMuteDate), any(AServerAChannelMessage.class))).thenReturn(createdMute);
        when(templateService.renderTemplate(eq(MuteServiceBean.MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class))).thenReturn(NOTIFICATION_TEXT);
    }

    private void verifyDirectMute() {
        verify(messageService, times(1)).sendMessageToUser(jdaUserBeingMuted, NOTIFICATION_TEXT, channel);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(roleService, times(1)).addRoleToUser(userBeingMuted, aRole);
        verify(executorService, times(1)).schedule(any(Runnable.class), anyLong(), any());
    }

    private Instant longerMute() {
        return Instant.now().plus(Duration.ofHours(1));
    }

    private Instant shorterMute() {
        return Instant.now().plus(Duration.ofSeconds(4));
    }

}
