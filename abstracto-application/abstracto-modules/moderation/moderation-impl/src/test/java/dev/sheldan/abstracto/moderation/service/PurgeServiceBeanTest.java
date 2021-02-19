package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.metrics.service.MetricService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.moderation.exception.NoMessageFoundException;
import dev.sheldan.abstracto.moderation.models.template.commands.PurgeStatusUpdateModel;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.TimeUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static dev.sheldan.abstracto.core.test.MockUtils.mockQueueDoubleVoidConsumer;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PurgeServiceBeanTest {

    @InjectMocks
    private PurgeServiceBean testUnit;

    @Mock
    private ChannelService channelService;

    @Mock
    private MessageService messageService;

    @Mock
    private TemplateService templateService;

    @Mock
    private MetricService metricService;

    @Mock
    private TextChannel textChannel;

    @Mock
    private Member purgedMember;

    private static final Long START_MESSAGE_ID = 4L;
    private static final Long STATUS_MESSAGE_ID = 7L;
    private static final Long AUTHOR_ID = 17L;

    @Mock
    private User messageAuthor;

    @Mock
    private Message firstMessage;

    @Mock
    private Message secondMessage;

    @Mock
    private Message thirdMessage;

    @Mock
    private Message fourthMessage;

    @Mock
    private MessageHistory history;

    @Mock
    private MessageToSend firstStatusUpdateMessage;

    @Mock
    private RestAction deleteMessagesAction;

    @Mock
    private AuditableRestAction deleteStatusAction;

    @Mock
    private Guild guild;

    private static final Long SERVER_ID = 1L;

    @Before
    public void setup() {
        when(textChannel.getGuild()).thenReturn(guild);
        when(guild.getId()).thenReturn(SERVER_ID.toString());
        when(guild.getIdLong()).thenReturn(SERVER_ID);
    }

    @Test
    public void testPurgeMessageViaStartMessage() {
        Integer amountToDelete = 50;
        when(channelService.getHistoryOfChannel(textChannel, START_MESSAGE_ID, amountToDelete)).thenReturn(CompletableFuture.completedFuture(history));

        setupOneMessageBatch(getDeletableMessageId(), getDeletableMessageId());

        Message messageToStartOffAT = Mockito.mock(Message.class);
        when(messageToStartOffAT.getIdLong()).thenReturn(START_MESSAGE_ID);
        CompletableFuture<Void> futures = testUnit.purgeMessagesInChannel(amountToDelete, textChannel, messageToStartOffAT, purgedMember);
        futures.whenComplete((aVoid, throwable) -> Assert.assertNull(throwable));
        verify(deleteStatusAction, times(1)).queueAfter(5, TimeUnit.SECONDS);
        verify(messageService, times(1)).updateStatusMessage(eq(textChannel), anyLong(), any());
        verify(metricService, times(1)).incrementCounter(any(), eq((long) amountToDelete));
    }

    @Test
    public void testPurgeMessageNotNoUser() {
        Integer amountToDelete = 50;
        when(channelService.getHistoryOfChannel(textChannel, START_MESSAGE_ID, amountToDelete)).thenReturn(CompletableFuture.completedFuture(history));

        when(firstMessage.getId()).thenReturn(getDeletableMessageId().toString());
        when(secondMessage.getId()).thenReturn(getDeletableMessageId().toString());

        setupFirstMessageHistoryMocks();
        setupStatusMessageMocks();
        mockQueueDoubleVoidConsumer(deleteMessagesAction);
        CompletableFuture<Void> futures = testUnit.purgeMessagesInChannel(amountToDelete, textChannel, START_MESSAGE_ID, null);
        futures.whenComplete((aVoid, throwable) -> Assert.assertNull(throwable));
        verify(deleteStatusAction, times(1)).queueAfter(5, TimeUnit.SECONDS);
        verify(messageService, times(1)).updateStatusMessage(eq(textChannel), anyLong(), any());
        verify(metricService, times(1)).incrementCounter(any(), eq((long) amountToDelete));
    }

    @Test
    public void testPurgeSingleMessage() {
        Integer amountToDelete = 50;
        when(channelService.getHistoryOfChannel(textChannel, START_MESSAGE_ID, amountToDelete)).thenReturn(CompletableFuture.completedFuture(history));

        when(firstMessage.getId()).thenReturn(getDeletableMessageId().toString());
        when(firstMessage.getAuthor()).thenReturn(messageAuthor);
        setupMembersWithAuthorId();

        List<Message> messagesToDelete = Arrays.asList(firstMessage);
        when(history.getRetrievedHistory()).thenReturn(messagesToDelete);
        setupStatusMessageMocks();
        AuditableRestAction auditableRestAction = Mockito.mock(AuditableRestAction.class);
        when(messageService.deleteMessageWithAction(firstMessage)).thenReturn(auditableRestAction);
        mockQueueDoubleVoidConsumer(auditableRestAction);
        CompletableFuture<Void> futures = testUnit.purgeMessagesInChannel(amountToDelete, textChannel, START_MESSAGE_ID, purgedMember);
        futures.whenComplete((aVoid, throwable) -> Assert.assertNull(throwable));
        verify(deleteStatusAction, times(1)).queueAfter(5, TimeUnit.SECONDS);
        verify(messageService, times(1)).updateStatusMessage(eq(textChannel), anyLong(), any());
        verify(metricService, times(1)).incrementCounter(any(), eq((long) amountToDelete));
    }

    @Test
    public void testPurgeMessagesInTwoIterationsSecondIterationsTooOld() {
        Integer amountToDelete = 150;
        Long latestDeletedMessageId = getDeletableMessageId();
        when(channelService.getHistoryOfChannel(textChannel, START_MESSAGE_ID, amountToDelete - 50)).thenReturn(CompletableFuture.completedFuture(history));

        MessageHistory secondHistory = Mockito.mock(MessageHistory.class);
        when(channelService.getHistoryOfChannel(textChannel, latestDeletedMessageId, 50)).thenReturn(CompletableFuture.completedFuture(secondHistory));

        when(secondMessage.getIdLong()).thenReturn(latestDeletedMessageId);
        when(thirdMessage.getId()).thenReturn(getNotDeletableMessageId().toString());
        when(fourthMessage.getId()).thenReturn(getNotDeletableMessageId().toString());

        setupOneMessageBatch(getDeletableMessageId(), latestDeletedMessageId);

        List<Message> secondMessagesToDelete = Arrays.asList(thirdMessage, fourthMessage);
        when(secondHistory.getRetrievedHistory()).thenReturn(secondMessagesToDelete);


        CompletableFuture<Void> futures = testUnit.purgeMessagesInChannel(amountToDelete, textChannel, START_MESSAGE_ID, purgedMember);
        futures.whenComplete((aVoid, throwable) -> Assert.assertTrue(throwable.getCause() instanceof NoMessageFoundException));
        verify(deleteStatusAction, times(1)).queueAfter(5, TimeUnit.SECONDS);
        verify(messageService, times(1)).updateStatusMessage(eq(textChannel), anyLong(), any());
        ArgumentCaptor<Long> deleted = ArgumentCaptor.forClass(Long.class);
        verify(metricService, times(2)).incrementCounter(any(), deleted.capture());
        List<Long> capturedValues = deleted.getAllValues();
        Assert.assertEquals(2, capturedValues.size());
        Assert.assertEquals(100, capturedValues.get(0).longValue());
        Assert.assertEquals(50, capturedValues.get(1).longValue());
    }


    @Test
    public void testPurgeMessagesInTwoIterations() {
        Integer amountToDelete = 150;
        Long latestDeletedMessageId = getDeletableMessageId();
        when(channelService.getHistoryOfChannel(textChannel, START_MESSAGE_ID, amountToDelete - 50)).thenReturn(CompletableFuture.completedFuture(history));
        MessageHistory secondHistory = Mockito.mock(MessageHistory.class);
        when(channelService.getHistoryOfChannel(textChannel, latestDeletedMessageId, 50)).thenReturn(CompletableFuture.completedFuture(secondHistory));

        when(secondMessage.getIdLong()).thenReturn(latestDeletedMessageId);

        setupOneMessageBatch(getDeletableMessageId(), latestDeletedMessageId);

        setupFirstMessages(thirdMessage, getDeletableMessageId(), fourthMessage, latestDeletedMessageId, messageAuthor);

        RestAction secondDeleteMessagesAction = Mockito.mock(RestAction.class);
        List<Message> secondMessagesToDelete = Arrays.asList(thirdMessage, fourthMessage);
        when(secondHistory.getRetrievedHistory()).thenReturn(secondMessagesToDelete);
        when(channelService.deleteMessagesInChannel(textChannel, secondMessagesToDelete)).thenReturn(secondDeleteMessagesAction);


        mockQueueDoubleVoidConsumer(secondDeleteMessagesAction);
        CompletableFuture<Void> futures = testUnit.purgeMessagesInChannel(amountToDelete, textChannel, START_MESSAGE_ID, purgedMember);
        futures.whenComplete((aVoid, throwable) -> Assert.assertNull(throwable));
        verify(deleteStatusAction, times(1)).queueAfter(5, TimeUnit.SECONDS);
        verify(messageService, times(2)).updateStatusMessage(eq(textChannel), anyLong(), any());
        ArgumentCaptor<Long> deleted = ArgumentCaptor.forClass(Long.class);
        verify(metricService, times(2)).incrementCounter(any(), deleted.capture());
        List<Long> capturedValues = deleted.getAllValues();
        Assert.assertEquals(2, capturedValues.size());
        Assert.assertEquals(100, capturedValues.get(0).longValue());
        Assert.assertEquals(50, capturedValues.get(1).longValue());
    }

    @Test
    public void testPurgeMessagesInOneIteration() {
        Integer amountToDelete = 50;
        when(channelService.getHistoryOfChannel(textChannel, START_MESSAGE_ID, amountToDelete)).thenReturn(CompletableFuture.completedFuture(history));

        setupOneMessageBatch(getDeletableMessageId(), getDeletableMessageId());

        CompletableFuture<Void> futures = testUnit.purgeMessagesInChannel(amountToDelete, textChannel, START_MESSAGE_ID, purgedMember);
        futures.whenComplete((aVoid, throwable) -> Assert.assertNull(throwable));
        verify(deleteStatusAction, times(1)).queueAfter(5, TimeUnit.SECONDS);
        verify(messageService, times(1)).updateStatusMessage(eq(textChannel), anyLong(), any());
        verify(metricService, times(1)).incrementCounter(any(), eq((long) amountToDelete));
    }

    @Test
    public void testPurgeTooOldMessage() {
        Integer amountToDelete = 50;
        when(channelService.getHistoryOfChannel(textChannel, START_MESSAGE_ID, amountToDelete)).thenReturn(CompletableFuture.completedFuture(history));

        when(firstMessage.getId()).thenReturn(getNotDeletableMessageId().toString());

        when(history.getRetrievedHistory()).thenReturn(Arrays.asList(firstMessage));
        setupStatusMessageMocks();
        CompletableFuture<Void> futures = testUnit.purgeMessagesInChannel(amountToDelete, textChannel, START_MESSAGE_ID, purgedMember);
        futures.whenComplete((aVoid, throwable) -> Assert.assertTrue(throwable.getCause() instanceof NoMessageFoundException));
    }

    private void setupOneMessageBatch(Long deletableMessageId, Long deletableMessageId2) {
        setupFirstMessages(firstMessage, deletableMessageId, secondMessage, deletableMessageId2, messageAuthor);
        setupMembersWithAuthorId();
        setupFirstMessageHistoryMocks();
        mockQueueDoubleVoidConsumer(deleteMessagesAction);
        setupStatusMessageMocks();
    }

    private void setupFirstMessageHistoryMocks() {
        List<Message> messagesToDelete = Arrays.asList(firstMessage, secondMessage);
        when(history.getRetrievedHistory()).thenReturn(messagesToDelete);
        when(channelService.deleteMessagesInChannel(textChannel, messagesToDelete)).thenReturn(deleteMessagesAction);
    }

    private void setupStatusMessageMocks() {
        when(templateService.renderTemplateToMessageToSend(eq("purge_status_update"), any(PurgeStatusUpdateModel.class), eq(SERVER_ID))).thenReturn(firstStatusUpdateMessage);
        when(messageService.createStatusMessageId(firstStatusUpdateMessage, textChannel)).thenReturn(CompletableFuture.completedFuture(STATUS_MESSAGE_ID));
        when(textChannel.deleteMessageById(STATUS_MESSAGE_ID)).thenReturn(deleteStatusAction);
    }

    private void setupMembersWithAuthorId() {
        when(messageAuthor.getIdLong()).thenReturn(AUTHOR_ID);
        when(purgedMember.getIdLong()).thenReturn(AUTHOR_ID);
    }

    private void setupFirstMessages(Message firstMessageToMock, Long firstMessageId, Message secondMessageToMock, Long secondMessageId, User author) {
        when(firstMessageToMock.getId()).thenReturn(firstMessageId.toString());
        when(firstMessageToMock.getAuthor()).thenReturn(author);
        when(secondMessageToMock.getId()).thenReturn(secondMessageId.toString());
        when(secondMessageToMock.getAuthor()).thenReturn(author);
    }



    private Long getDeletableMessageId() {
        return TimeUtil.getDiscordTimestamp((System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)));
    }

    private Long getNotDeletableMessageId() {
        return TimeUtil.getDiscordTimestamp((System.currentTimeMillis() - (21 * 24 * 60 * 60 * 1000)));
    }

}
