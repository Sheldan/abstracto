package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.execution.result.MessageReceivedListenerResult;
import dev.sheldan.abstracto.core.metrics.service.MetricService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.features.mode.InviteFilterMode;
import dev.sheldan.abstracto.moderation.config.posttargets.InviteFilterPostTarget;
import dev.sheldan.abstracto.moderation.models.template.listener.DeletedInvitesNotificationModel;
import dev.sheldan.abstracto.moderation.service.InviteLinkFilterService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.listener.async.InviteLinkFilterListener.INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InviteLinkFilterListenerTest {

    @InjectMocks
    private InviteLinkFilterListener testUnit;

    @Mock
    private InviteLinkFilterService inviteLinkFilterService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private TemplateService templateService;

    @Mock
    private MessageService messageService;

    @Mock
    private Message message;

    @Mock
    private User author;

    @Mock
    private TextChannel textChannel;

    @Mock
    private Guild guild;

    @Mock
    private MetricService metricService;

    private static final Long SERVER_ID = 1L;
    private static final Long CHANNEL_ID = 2L;
    private static final Long USER_ID = 3L;
    private static final Long MESSAGE_ID = 4L;
    private static final String INVITE_CODE = "asdf";
    private static final String INVITE_LINK = "discord.gg/" + INVITE_CODE;

    @Test
    public void testExecutionWithNoInvite() {
        when(message.getContentRaw()).thenReturn("text");
        setupBasicMessage();
        MessageReceivedListenerResult result = testUnit.execute(message);
        Assert.assertEquals(MessageReceivedListenerResult.PROCESSED, result);
    }

    @Test
    public void testExecutionWithOneAllowedInvite() {
        when(message.getContentRaw()).thenReturn(INVITE_LINK);
        when(inviteLinkFilterService.isCodeFiltered(eq(INVITE_CODE), any(ServerUser.class))).thenReturn(false);
        setupBasicMessage();
        MessageReceivedListenerResult result = testUnit.execute(message);
        Assert.assertEquals(MessageReceivedListenerResult.PROCESSED, result);
    }

    @Test
    public void testExecutionWithOneNotAllowedInviteNoTrackNoNotification() {
        when(message.getContentRaw()).thenReturn(INVITE_LINK);
        when(messageService.deleteMessage(message)).thenReturn(CompletableFuture.completedFuture(null));
        when(inviteLinkFilterService.isCodeFiltered(eq(INVITE_CODE), any(ServerUser.class))).thenReturn(true);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.TRACK_USES)).thenReturn(false);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.FILTER_NOTIFICATIONS)).thenReturn(false);
        setupBasicMessage();
        MessageReceivedListenerResult result = testUnit.execute(message);
        Assert.assertEquals(MessageReceivedListenerResult.DELETED, result);
        verify(metricService, times(1)).incrementCounter(any());
    }

    @Test
    public void testExecutionWithOneNotAllowedInviteTrackNoNotification() {
        when(message.getContentRaw()).thenReturn(INVITE_LINK);
        when(messageService.deleteMessage(message)).thenReturn(CompletableFuture.completedFuture(null));
        when(inviteLinkFilterService.isCodeFiltered(eq(INVITE_CODE), any(ServerUser.class))).thenReturn(true);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.TRACK_USES)).thenReturn(true);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.FILTER_NOTIFICATIONS)).thenReturn(false);
        setupBasicMessage();
        MessageReceivedListenerResult result = testUnit.execute(message);
        Assert.assertEquals(MessageReceivedListenerResult.DELETED, result);
        verifyTracking();
        verify(metricService, times(1)).incrementCounter(any());
    }

    @Test
    public void testExecutionWithOneNotAllowedInviteTrackNotification() {
        when(message.getContentRaw()).thenReturn(INVITE_LINK);
        when(messageService.deleteMessage(message)).thenReturn(CompletableFuture.completedFuture(null));
        when(inviteLinkFilterService.isCodeFiltered(eq(INVITE_CODE), any(ServerUser.class))).thenReturn(true);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.TRACK_USES)).thenReturn(true);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.FILTER_NOTIFICATIONS)).thenReturn(true);
        setupForNotification();
        setupBasicMessage();
        MessageReceivedListenerResult result = testUnit.execute(message);
        Assert.assertEquals(MessageReceivedListenerResult.DELETED, result);
        verifyTracking();
        verify(metricService, times(1)).incrementCounter(any());
    }

    @Test
    public void testExecutionWithOneNotAllowedInviteNoTrackNotification() {
        when(message.getContentRaw()).thenReturn(INVITE_LINK);
        when(messageService.deleteMessage(message)).thenReturn(CompletableFuture.completedFuture(null));
        when(inviteLinkFilterService.isCodeFiltered(eq(INVITE_CODE), any(ServerUser.class))).thenReturn(true);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.TRACK_USES)).thenReturn(false);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.FILTER_NOTIFICATIONS)).thenReturn(true);
        setupForNotification();
        setupBasicMessage();
        MessageReceivedListenerResult result = testUnit.execute(message);
        verify(metricService, times(1)).incrementCounter(any());
        Assert.assertEquals(MessageReceivedListenerResult.DELETED, result);
        verify(inviteLinkFilterService, times(0)).storeFilteredInviteLinkUsage(eq(INVITE_CODE), any(ServerUser.class));
    }

    @Test
    public void testExecutionWithOneNotAllowedInviteNoTrackNotificationNoPostTarget() {
        when(message.getContentRaw()).thenReturn(INVITE_LINK);
        when(messageService.deleteMessage(message)).thenReturn(CompletableFuture.completedFuture(null));
        when(inviteLinkFilterService.isCodeFiltered(eq(INVITE_CODE), any(ServerUser.class))).thenReturn(true);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.TRACK_USES)).thenReturn(false);
        when(featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, SERVER_ID, InviteFilterMode.FILTER_NOTIFICATIONS)).thenReturn(true);
        when(postTargetService.postTargetDefinedInServer(InviteFilterPostTarget.INVITE_DELETE_LOG, SERVER_ID)).thenReturn(false);
        setupBasicMessage();
        MessageReceivedListenerResult result = testUnit.execute(message);
        verify(metricService, times(1)).incrementCounter(any());
        Assert.assertEquals(MessageReceivedListenerResult.DELETED, result);
        verify(inviteLinkFilterService, times(0)).storeFilteredInviteLinkUsage(eq(INVITE_CODE), any(ServerUser.class));
        verify(templateService, times(0)).renderEmbedTemplate(eq(INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY), any(DeletedInvitesNotificationModel.class));
    }

    private void verifyTracking() {
        verify(inviteLinkFilterService, times(1)).storeFilteredInviteLinkUsage(eq(INVITE_CODE), any(ServerUser.class));
    }

    private void setupForNotification() {
        when(postTargetService.postTargetDefinedInServer(InviteFilterPostTarget.INVITE_DELETE_LOG, SERVER_ID)).thenReturn(true);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY), any(DeletedInvitesNotificationModel.class))).thenReturn(messageToSend);
        when(postTargetService.sendEmbedInPostTarget(messageToSend, InviteFilterPostTarget.INVITE_DELETE_LOG, SERVER_ID)).thenReturn(CommandTestUtilities.messageFutureList());
        when(textChannel.getIdLong()).thenReturn(CHANNEL_ID);
        when(message.getIdLong()).thenReturn(MESSAGE_ID);
        when(message.getTextChannel()).thenReturn(textChannel);
    }

    private void setupBasicMessage() {
        when(message.getAuthor()).thenReturn(author);
        when(author.getIdLong()).thenReturn(USER_ID);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
    }
}
