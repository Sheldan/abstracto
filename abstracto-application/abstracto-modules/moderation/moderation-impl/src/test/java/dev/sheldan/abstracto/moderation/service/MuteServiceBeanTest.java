package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.posttargets.MutingPostTarget;
import dev.sheldan.abstracto.moderation.exception.MuteRoleNotSetupException;
import dev.sheldan.abstracto.moderation.exception.NoMuteFoundException;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.models.database.MuteRole;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteContext;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import static dev.sheldan.abstracto.moderation.service.MuteServiceBean.MUTE_NOTIFICATION_TEMPLATE;
import static dev.sheldan.abstracto.moderation.service.MuteServiceBean.UN_MUTE_LOG_TEMPLATE;
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
    private MuteServiceBean self;

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
    private ServerChannelMessage cause;

    @Mock
    private Guild guild;

    @Mock
    private ARole aRole;

    @Mock
    private MuteRole muteRole;

    @Mock
    private MessageToSend messageToSend;

    @Mock
    private Mute mute;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private CounterService counterService;

    private static final Long CHANNEL_ID = 8L;
    private static final String REASON = "reason";
    private static final String NOTIFICATION_TEXT = "text";
    private static final String TRIGGER = "trigger";
    public static final long MUTE_ID = 6L;
    public static final long SERVER_ID = 7L;

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
        cause = Mockito.mock(ServerChannelMessage.class);
        aRole = ARole.builder().build();
        muteRole = MuteRole.builder().role(aRole).build();
        messageToSend = Mockito.mock(MessageToSend.class);
    }

    @Test
    public void testMuteUserWithScheduler() {
        Instant unMuteDate = longerMute();
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        FullUserInServer mutedUser = FullUserInServer.builder().member(memberBeingMuted).aUserInAServer(userBeingMuted).build();
        FullUserInServer mutingUser = FullUserInServer.builder().member(memberMuting).aUserInAServer(userMuting).build();
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class))).thenReturn(NOTIFICATION_TEXT);

        when(messageService.sendMessageToUser(jdaUserBeingMuted, NOTIFICATION_TEXT)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToUserFuture(userBeingMuted, aRole)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.muteUserInServer(mutedUser, mutingUser, REASON, unMuteDate, cause);
    }

    @Test
    public void testMuteWithDirectUnMute() {
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        FullUserInServer mutedUser = FullUserInServer.builder().member(memberBeingMuted).aUserInAServer(userBeingMuted).build();
        FullUserInServer mutingUser = FullUserInServer.builder().member(memberMuting).aUserInAServer(userMuting).build();
        Instant unMuteDate = shorterMute();
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);

        String notificationText = "text";
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class))).thenReturn(notificationText);
        when(messageService.sendMessageToUser(memberBeingMuted.getUser(), notificationText)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToUserFuture(userBeingMuted, muteRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.muteUserInServer(mutedUser, mutingUser, REASON, unMuteDate, cause);
        verifyDirectMute();
    }

    @Test(expected = MuteRoleNotSetupException.class)
    public void testMuteUserWithoutMuteRole() {
        FullUserInServer mutedUser = FullUserInServer.builder().aUserInAServer(userBeingMuted).build();
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(false);
        testUnit.muteUserInServer(mutedUser, FullUserInServer.builder().build(), REASON, longerMute(), Mockito.mock(ServerChannelMessage.class));
    }

    @Test
    public void testCancelUnMuteJob() {
        when(mute.getTriggerKey()).thenReturn(TRIGGER);
        testUnit.cancelUnMuteJob(mute);
        verify(schedulerService, times(1)).stopTrigger(TRIGGER);
    }

    @Test
    public void testCancelNotExistingJob() {
        testUnit.cancelUnMuteJob(mute);
        verify(schedulerService, times(0)).stopTrigger(anyString());
    }

    @Test
    public void testMuteMember() {
        when(userInServerManagementService.loadUser(memberBeingMuted)).thenReturn(userBeingMuted);
        when(userInServerManagementService.loadUser(memberMuting)).thenReturn(userMuting);
        Instant unMuteDate = shorterMute();
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);

        String notificationText = "text";
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class))).thenReturn(notificationText);
        when(messageService.sendMessageToUser(memberBeingMuted.getUser(), notificationText)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToUserFuture(userBeingMuted, muteRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.muteMember(memberBeingMuted, memberMuting, REASON, unMuteDate, cause);
        verifyDirectMute();
    }

    @Test
    public void testMuteMemberWithLog() {
        when(userInServerManagementService.loadUser(memberBeingMuted)).thenReturn(userBeingMuted);
        when(userInServerManagementService.loadUser(memberMuting)).thenReturn(userMuting);
        Instant unMuteDate = shorterMute();
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);

        ServerChannelMessage serverChannelMessage = Mockito.mock(ServerChannelMessage.class);
        when(serverChannelMessage.getServerId()).thenReturn(SERVER_ID);
        MuteContext muteLog = Mockito.mock(MuteContext.class);
        when(muteLog.getMutedUser()).thenReturn(memberBeingMuted);
        when(muteLog.getMutingUser()).thenReturn(memberMuting);
        when(muteLog.getContext()).thenReturn(serverChannelMessage);
        when(muteLog.getMuteTargetDate()).thenReturn(unMuteDate);
        String notificationText = "text";
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class))).thenReturn(notificationText);
        when(messageService.sendMessageToUser(memberBeingMuted.getUser(), notificationText)).thenReturn(CompletableFuture.completedFuture(null));
        when(templateService.renderEmbedTemplate(eq(MuteServiceBean.MUTE_LOG_TEMPLATE), any(MuteContext.class))).thenReturn(messageToSend);
        when(roleService.addRoleToUserFuture(userBeingMuted, muteRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.muteMemberWithLog(muteLog);
        verifyDirectMute();
        verify(templateService, times(1)).renderEmbedTemplate(eq(MuteServiceBean.MUTE_LOG_TEMPLATE), any(MuteContext.class));
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, MutingPostTarget.MUTE_LOG, SERVER_ID);
    }

    @Test
    public void testUnMuteMemberWhoseMuteEnded() {
        when(mute.getMuteEnded()).thenReturn(true);
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(muteManagementService.getAMuteOf(userBeingMuted)).thenReturn(mute);
        when(mute.getMuteId()).thenReturn(new ServerSpecificId(SERVER_ID, MUTE_ID));
        testUnit.unMuteUser(userBeingMuted);
        verifyNoUnMuteHappened();
    }

    @Test
    public void testEndMute() {
        setupUnMuteMocks(true);
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(mute.getMutingUser()).thenReturn(userMuting);
        when(mute.getServer()).thenReturn(server);
        when(muteManagementService.findMuteOptional(MUTE_ID, SERVER_ID)).thenReturn(Optional.of(mute));
        when(roleService.removeRoleFromUserFuture(userBeingMuted, aRole)).thenReturn(CompletableFuture.completedFuture(null));
        when(botService.getMemberInServerAsync(userBeingMuted)).thenReturn(CompletableFuture.completedFuture(memberBeingMuted));
        when(botService.getMemberInServerAsync(userMuting)).thenReturn(CompletableFuture.completedFuture(memberMuting));
        testUnit.endMute(MUTE_ID, SERVER_ID);
        verify(self, times(1)).sendUnmuteLog(eq(MUTE_ID), any(Guild.class), any(CompletableFuture.class), any(CompletableFuture.class));
    }

    @Test
    public void testSendUnmuteLog() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(muteManagementService.findMute(MUTE_ID, SERVER_ID)).thenReturn(mute);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(templateService.renderEmbedTemplate(eq(UN_MUTE_LOG_TEMPLATE), any(UnMuteLog.class))).thenReturn(messageToSend);
        when(postTargetService.sendEmbedInPostTarget(eq(messageToSend), eq(MutingPostTarget.MUTE_LOG), anyLong())).thenReturn(Arrays.asList(CompletableFuture.completedFuture(null)));
        testUnit.sendUnmuteLog(MUTE_ID, guild, CompletableFuture.completedFuture(memberMuting), CompletableFuture.completedFuture(memberBeingMuted));
        verify(self, times(1)).endMuteInDatabase(MUTE_ID, SERVER_ID);
    }

    @Test(expected = NoMuteFoundException.class)
    public void testEndNonExistingMute() {
        when(muteManagementService.findMuteOptional(MUTE_ID, SERVER_ID)).thenReturn(Optional.empty());
        testUnit.endMute(MUTE_ID, SERVER_ID);
    }

    @Test
    public void testUnMuteMemberInGuild() {
        executeUnMuteWithLogTest(true);
    }

    @Test
    public void testUnMuteMemberWhoLeftGuild() {
        executeUnMuteWithLogTest(false);
    }

    @Test
    public void testCompletelyUnMuteNotMutedUser() {
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList());
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(0)).saveMute(any(Mute.class));
    }

    @Test
    public void testCompletelyUnMuteNotScheduledMuteUser() {
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(0)).stopTrigger(anyString());
    }

    @Test
    public void testCompletelyUnMuteScheduledMuteUser() {
        when(mute.getTriggerKey()).thenReturn(TRIGGER);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(1)).stopTrigger(TRIGGER);
    }

    @Test
    public void testCompletelyUnMuteMember() {
        when(mute.getTriggerKey()).thenReturn(TRIGGER);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        when(userInServerManagementService.loadUser(memberBeingMuted)).thenReturn(userBeingMuted);
        testUnit.completelyUnMuteMember(memberBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(1)).stopTrigger(TRIGGER);
    }

    @Test
    public void verifyScheduling() {
        Instant unMuteDate = shorterMute();
        MuteContext muteLog = Mockito.mock(MuteContext.class);
        when(muteLog.getMuteTargetDate()).thenReturn(unMuteDate);
        when(muteLog.getMuteId()).thenReturn(MUTE_ID);
        ServerChannelMessage serverContext = Mockito.mock(ServerChannelMessage.class);
        when(serverContext.getServerId()).thenReturn(SERVER_ID);
        when(serverContext.getChannelId()).thenReturn(CHANNEL_ID);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(aChannel);
        when(muteLog.getContext()).thenReturn(serverContext);
        testUnit.persistMute(muteLog);
        verify(executorService, times(1)).schedule(any(Runnable.class), anyLong(), any());
    }

    private void verifyNoUnMuteHappened() {
        verify(muteManagementService, times(0)).saveMute(any(Mute.class));
        verify(roleService, times(0)).removeRoleFromUser(eq(userBeingMuted), any(ARole.class));
        verify(postTargetService, times(0)).sendEmbedInPostTarget(any(MessageToSend.class), eq(MutingPostTarget.MUTE_LOG), eq(server.getId()));
    }

    private void executeUnMuteWithLogTest(boolean stillInGuild) {
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(mute.getMutingUser()).thenReturn(userMuting);
        when(mute.getServer()).thenReturn(server);
        setupUnMuteMocks(stillInGuild);
        when(roleService.removeRoleFromUserFuture(userBeingMuted, aRole)).thenReturn(CompletableFuture.completedFuture(null));
        when(botService.getMemberInServerAsync(userBeingMuted)).thenReturn(CompletableFuture.completedFuture(memberBeingMuted));
        when(botService.getMemberInServerAsync(userMuting)).thenReturn(CompletableFuture.completedFuture(memberMuting));
        testUnit.unMuteUser(userBeingMuted);

    }

    private void setupUnMuteMocks(boolean stillInGuild) {
        when(mute.getMuteId()).thenReturn(new ServerSpecificId(SERVER_ID, MUTE_ID));
        when(muteManagementService.getAMuteOf(userBeingMuted)).thenReturn(mute);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(botService.getGuildById(server.getId())).thenReturn(guild);
        when(botService.isUserInGuild(guild, userBeingMuted)).thenReturn(stillInGuild);
    }

    private void verifyDirectMute() {
        verify(messageService, times(1)).sendMessageToUser(jdaUserBeingMuted, NOTIFICATION_TEXT);
    }

    private Instant longerMute() {
        return Instant.now().plus(Duration.ofHours(1));
    }

    private Instant shorterMute() {
        return Instant.now().plus(Duration.ofSeconds(4));
    }

}
