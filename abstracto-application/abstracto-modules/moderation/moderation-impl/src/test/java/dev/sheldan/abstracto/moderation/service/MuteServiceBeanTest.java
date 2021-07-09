package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.posttarget.MutingPostTarget;
import dev.sheldan.abstracto.moderation.exception.MuteRoleNotSetupException;
import dev.sheldan.abstracto.moderation.exception.NoMuteFoundException;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.model.database.MuteRole;
import dev.sheldan.abstracto.moderation.model.template.command.MuteContext;
import dev.sheldan.abstracto.moderation.model.template.command.MuteNotification;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import dev.sheldan.abstracto.moderation.service.management.MuteRoleManagementService;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
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
    private GuildService guildService;

    @Mock
    private MemberService memberService;

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
    private AUser user;

    @Mock
    private Mute mute;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private CounterService counterService;

    @Mock
    private FeatureModeService featureModeService;

    private static final Long CHANNEL_ID = 8L;
    private static final String REASON = "reason";
    private static final String NOTIFICATION_TEXT = "text";
    private static final String TRIGGER = "trigger";
    public static final Long MUTE_ID = 6L;
    public static final Long SERVER_ID = 7L;
    public static final Long USER_MUTING_ID = 4L;
    public static final Long USER_BEING_MUTED_ID = 3L;

    @Test
    public void testMuteUserWithScheduler() {
        Instant unMuteDate = longerMute();
        when(cause.getServerId()).thenReturn(SERVER_ID);
        FullUserInServer mutedUser = Mockito.mock(FullUserInServer.class);
        FullUserInServer mutingUser = Mockito.mock(FullUserInServer.class);
        setupFullUsers(mutedUser, mutingUser);
        when(muteRole.getRole()).thenReturn(aRole);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class), eq(SERVER_ID))).thenReturn(NOTIFICATION_TEXT);

        when(messageService.sendMessageToUser(jdaUserBeingMuted, NOTIFICATION_TEXT)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToUserAsync(userBeingMuted, aRole)).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Void> future = testUnit.muteUserInServer(mutedUser, mutingUser, REASON, unMuteDate, cause);
        future.join();
        Assert.assertFalse(future.isCompletedExceptionally());
    }

    private void setupFullUsers(FullUserInServer mutedUser, FullUserInServer mutingUser) {
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(mutedUser.getAUserInAServer()).thenReturn(userBeingMuted);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(mutedUser.getMember()).thenReturn(memberBeingMuted);
        when(mutingUser.getMember()).thenReturn(memberMuting);
        when(memberBeingMuted.getGuild()).thenReturn(guild);
    }

    @Test
    public void testMuteWithDirectUnMute() {
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        FullUserInServer mutedUser = Mockito.mock(FullUserInServer.class);
        FullUserInServer mutingUser = Mockito.mock(FullUserInServer.class);
        setupFullUsers(mutedUser, mutingUser);
        Instant unMuteDate = shorterMute();
        when(cause.getServerId()).thenReturn(SERVER_ID);
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(muteRole.getRole()).thenReturn(aRole);
        String notificationText = "text";
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class), eq(SERVER_ID))).thenReturn(notificationText);
        when(messageService.sendMessageToUser(memberBeingMuted.getUser(), notificationText)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToUserAsync(userBeingMuted, muteRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Void> future = testUnit.muteUserInServer(mutedUser, mutingUser, REASON, unMuteDate, cause);
        future.join();
        Assert.assertFalse(future.isCompletedExceptionally() );
        verifyDirectMute();
    }

    @Test(expected = MuteRoleNotSetupException.class)
    public void testMuteUserWithoutMuteRole() {
        FullUserInServer mutedUser = Mockito.mock(FullUserInServer.class);
        when(mutedUser.getAUserInAServer()).thenReturn(userBeingMuted);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(false);
        FullUserInServer mutingUser = Mockito.mock(FullUserInServer.class);
        ServerChannelMessage serverChannelMessage = mock(ServerChannelMessage.class);
        testUnit.muteUserInServer(mutedUser, mutingUser, REASON, longerMute(), serverChannelMessage);
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
        when(userInServerManagementService.loadOrCreateUser(memberBeingMuted)).thenReturn(userBeingMuted);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(userInServerManagementService.loadOrCreateUser(memberMuting)).thenReturn(userMuting);
        Instant unMuteDate = shorterMute();
        when(cause.getServerId()).thenReturn(SERVER_ID);
        when(memberBeingMuted.getGuild()).thenReturn(guild);
        when(memberBeingMuted.getUser()).thenReturn(jdaUserBeingMuted);
        when(muteRoleManagementService.muteRoleForServerExists(server)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);

        String notificationText = "text";
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class), eq(SERVER_ID))).thenReturn(notificationText);
        when(messageService.sendMessageToUser(memberBeingMuted.getUser(), notificationText)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToUserAsync(userBeingMuted, muteRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.muteMember(memberBeingMuted, memberMuting, REASON, unMuteDate, cause);
        verifyDirectMute();
    }

    @Test
    public void testMuteMemberWithLog() {
        when(userInServerManagementService.loadOrCreateUser(memberBeingMuted)).thenReturn(userBeingMuted);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(userInServerManagementService.loadOrCreateUser(memberMuting)).thenReturn(userMuting);
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
        when(server.getId()).thenReturn(SERVER_ID);
        when(serverManagementService.loadOrCreate(SERVER_ID)).thenReturn(server);
        String notificationText = "text";
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class), eq(SERVER_ID))).thenReturn(notificationText);
        when(messageService.sendMessageToUser(memberBeingMuted.getUser(), notificationText)).thenReturn(CompletableFuture.completedFuture(null));
        when(templateService.renderEmbedTemplate(eq(MuteServiceBean.MUTE_LOG_TEMPLATE), any(MuteContext.class), eq(SERVER_ID))).thenReturn(messageToSend);
        when(roleService.addRoleToUserAsync(userBeingMuted, muteRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.muteMemberWithLog(muteLog);
        verifyDirectMute();
        verify(templateService, times(1)).renderEmbedTemplate(eq(MuteServiceBean.MUTE_LOG_TEMPLATE), any(MuteContext.class), eq(SERVER_ID));
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, MutingPostTarget.MUTE_LOG, SERVER_ID);
    }

    @Test
    public void testMuteMemberWithoutLog() {
        when(userInServerManagementService.loadOrCreateUser(memberBeingMuted)).thenReturn(userBeingMuted);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(userInServerManagementService.loadOrCreateUser(memberMuting)).thenReturn(userMuting);
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
        when(serverManagementService.loadOrCreate(SERVER_ID)).thenReturn(server);
        String notificationText = "text";
        when(templateService.renderTemplate(eq(MUTE_NOTIFICATION_TEMPLATE), any(MuteNotification.class), eq(SERVER_ID))).thenReturn(notificationText);
        when(messageService.sendMessageToUser(memberBeingMuted.getUser(), notificationText)).thenReturn(CompletableFuture.completedFuture(null));
        when(roleService.addRoleToUserAsync(userBeingMuted, muteRole.getRole())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.muteMemberWithLog(muteLog);
        verifyDirectMute();
        verify(postTargetService, times(0)).sendEmbedInPostTarget(messageToSend, MutingPostTarget.MUTE_LOG, SERVER_ID);
    }

    @Test
    public void testUnMuteMemberWhoseMuteEnded() {
        when(mute.getMuteEnded()).thenReturn(true);
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(muteManagementService.hasActiveMute(userBeingMuted)).thenReturn(true);
        when(muteManagementService.getAMuteOf(userBeingMuted)).thenReturn(mute);
        when(mute.getMuteId()).thenReturn(new ServerSpecificId(SERVER_ID, MUTE_ID));
        when(guildService.getGuildById(SERVER_ID)).thenReturn(guild);
        testUnit.unMuteUser(userBeingMuted);
        verifyNoUnMuteHappened();
    }

    @Test
    public void testEndMute() {
        setupUnMuteMocks();
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(userBeingMuted.getUserReference()).thenReturn(user);
        when(mute.getMutingUser()).thenReturn(userMuting);
        when(mute.getServer()).thenReturn(server);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(muteRole.getRole()).thenReturn(aRole);
        when(muteManagementService.findMuteOptional(MUTE_ID, SERVER_ID)).thenReturn(Optional.of(mute));
        when(roleService.removeRoleFromUserAsync(userBeingMuted, aRole)).thenReturn(CompletableFuture.completedFuture(null));
        when(memberService.getMemberInServerAsync(userBeingMuted)).thenReturn(CompletableFuture.completedFuture(memberBeingMuted));
        when(memberService.getMemberInServerAsync(userMuting)).thenReturn(CompletableFuture.completedFuture(memberMuting));
        testUnit.endMute(MUTE_ID, SERVER_ID);
        verify(self, times(1)).sendUnmuteLog(eq(MUTE_ID), any(Guild.class), any(CompletableFuture.class), any(CompletableFuture.class));
    }

    @Test
    public void testSendUnmuteLog() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(muteManagementService.findMute(MUTE_ID, SERVER_ID)).thenReturn(mute);
        when(mute.getMuteId()).thenReturn(new ServerSpecificId(SERVER_ID, MUTE_ID));
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
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
        executeUnMuteWithLogTest();
    }

    @Test
    public void testCompletelyUnMuteNotMutedUser() {
        when(userBeingMuted.getUserReference()).thenReturn(user);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList());
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(0)).saveMute(any(Mute.class));
    }

    @Test
    public void testCompletelyUnMuteNotScheduledMuteUser() {
        when(userBeingMuted.getUserReference()).thenReturn(user);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(0)).stopTrigger(anyString());
    }

    @Test
    public void testCompletelyUnMuteScheduledMuteUser() {
        when(mute.getTriggerKey()).thenReturn(TRIGGER);
        when(userBeingMuted.getUserReference()).thenReturn(user);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        testUnit.completelyUnMuteUser(userBeingMuted);
        verify(muteManagementService, times(1)).saveMute(any(Mute.class));
        verify(schedulerService, times(1)).stopTrigger(TRIGGER);
    }

    @Test
    public void testCompletelyUnMuteMember() {
        when(userBeingMuted.getUserReference()).thenReturn(user);
        when(userBeingMuted.getServerReference()).thenReturn(server);
        when(mute.getTriggerKey()).thenReturn(TRIGGER);
        when(muteManagementService.getAllMutesOf(userBeingMuted)).thenReturn(Arrays.asList(mute));
        when(userInServerManagementService.loadOrCreateUser(memberBeingMuted)).thenReturn(userBeingMuted);
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
        verify(postTargetService, times(0)).sendEmbedInPostTarget(any(MessageToSend.class), eq(MutingPostTarget.MUTE_LOG), eq(SERVER_ID));
    }

    private void executeUnMuteWithLogTest() {
        when(userBeingMuted.getUserReference()).thenReturn(user);
        when(mute.getMutedUser()).thenReturn(userBeingMuted);
        when(mute.getMutingUser()).thenReturn(userMuting);
        when(mute.getServer()).thenReturn(server);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(muteRole.getRole()).thenReturn(aRole);
        setupUnMuteMocks();
        when(roleService.removeRoleFromUserAsync(userBeingMuted, aRole)).thenReturn(CompletableFuture.completedFuture(null));
        when(memberService.getMemberInServerAsync(userBeingMuted)).thenReturn(CompletableFuture.completedFuture(memberBeingMuted));
        when(memberService.getMemberInServerAsync(userMuting)).thenReturn(CompletableFuture.completedFuture(memberMuting));
        testUnit.unMuteUser(userBeingMuted);

    }

    private void setupUnMuteMocks() {
        when(mute.getMuteId()).thenReturn(new ServerSpecificId(SERVER_ID, MUTE_ID));
        when(muteManagementService.getAMuteOf(userBeingMuted)).thenReturn(mute);
        when(muteManagementService.hasActiveMute(userBeingMuted)).thenReturn(true);
        when(muteRoleManagementService.retrieveMuteRoleForServer(server)).thenReturn(muteRole);
        when(guildService.getGuildById(server.getId())).thenReturn(guild);
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
