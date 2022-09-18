package dev.sheldan.abstracto.logging.listener;

import dev.sheldan.abstracto.core.models.cache.CachedAttachment;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageDeletedModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.logging.config.LoggingPostTarget;
import dev.sheldan.abstracto.logging.model.template.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.logging.model.template.MessageDeletedLog;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageDeleteLogListenerTest {
    @InjectMocks
    private MessageDeleteLogListener testUnit;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private ChannelService channelService;

    @Mock
    private MemberService memberService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private MessageDeleteLogListener self;

    @Captor
    private ArgumentCaptor<MessageDeletedLog> captor;

    @Captor
    private ArgumentCaptor<MessageDeletedAttachmentLog> attachmentCaptor;

    @Captor
    private ArgumentCaptor<MessageToSend> messageCaptor;

    private static final Long SERVER_ID = 1L;
    private static final Long AUTHOR_ID = 2L;
    private static final Long CHANNEL_ID = 3L;

    @Mock
    private CachedMessage deletedMessage;

    @Mock
    private CachedAuthor cachedAuthor;

    @Mock
    private TextChannel textChannel;

    @Mock
    private Member member;

    @Mock
    private Guild guild;

    @Mock
    private MessageDeletedModel model;

    @Test
    public void testExecuteListener() {
        when(deletedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(memberService.getMemberInServerAsync(SERVER_ID, AUTHOR_ID)).thenReturn(CompletableFuture.completedFuture(member));
        when(model.getCachedMessage()).thenReturn(deletedMessage);
        when(model.getServerId()).thenReturn(SERVER_ID);
        testUnit.execute(model);
        verify(self, times(1)).executeListener(deletedMessage, member);
    }

    @Test
    public void testExecuteListenerWithSimpleMessage() {
        when(deletedMessage.getServerId()).thenReturn(SERVER_ID);
        when(deletedMessage.getChannelId()).thenReturn(CHANNEL_ID);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(member.getGuild()).thenReturn(guild);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_TEMPLATE), captor.capture(), eq(SERVER_ID))).thenReturn(messageToSend);
        when(channelService.getMessageChannelFromServer(SERVER_ID, CHANNEL_ID)).thenReturn(textChannel);
        testUnit.executeListener(deletedMessage, member);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, LoggingPostTarget.DELETE_LOG, SERVER_ID);
        MessageDeletedLog messageDeletedLog = captor.getValue();
        Assert.assertEquals(deletedMessage, messageDeletedLog.getCachedMessage());
        Assert.assertEquals(guild, messageDeletedLog.getGuild());
        Assert.assertEquals(textChannel, messageDeletedLog.getChannel());
        Assert.assertEquals(member, messageDeletedLog.getMember());
    }

    @Test
    public void testExecuteListenerWithOneAttachment() {
        String attachmentUrl = "url";
        when(deletedMessage.getServerId()).thenReturn(SERVER_ID);
        when(deletedMessage.getChannelId()).thenReturn(CHANNEL_ID);
        when(channelService.getMessageChannelFromServer(SERVER_ID, CHANNEL_ID)).thenReturn(textChannel);
        CachedAttachment cachedAttachment = Mockito.mock(CachedAttachment.class);
        when(cachedAttachment.getProxyUrl()).thenReturn(attachmentUrl);
        List<CachedAttachment> attachmentList = Arrays.asList(cachedAttachment);
        when(deletedMessage.getAttachments()).thenReturn(attachmentList);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        MessageToSend attachmentMessage = Mockito.mock(MessageToSend.class);
        when(member.getGuild()).thenReturn(guild);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_TEMPLATE), captor.capture(), eq(SERVER_ID))).thenReturn(messageToSend);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_ATTACHMENT_TEMPLATE), attachmentCaptor.capture(), eq(SERVER_ID))).thenReturn(attachmentMessage);
        testUnit.executeListener(deletedMessage, member);
        verify(postTargetService, times(2)).sendEmbedInPostTarget(messageCaptor.capture(), eq(LoggingPostTarget.DELETE_LOG), eq(SERVER_ID));
        List<MessageToSend> messagesSent = messageCaptor.getAllValues();
        Assert.assertEquals(messageToSend, messagesSent.get(0));
        Assert.assertEquals(attachmentMessage, messagesSent.get(1));
        MessageDeletedAttachmentLog attachmentLog = attachmentCaptor.getValue();
        verifyAttachmentLog(attachmentUrl, attachmentLog, 1);
        verifyMessageDeletedLog();
    }

    @Test
    public void testExecuteListenerWithTwoAttachment() {
        when(deletedMessage.getServerId()).thenReturn(SERVER_ID);
        when(deletedMessage.getChannelId()).thenReturn(CHANNEL_ID);
        when(channelService.getMessageChannelFromServer(SERVER_ID, CHANNEL_ID)).thenReturn(textChannel);
        String attachmentUrl = "url";
        String secondAttachmentUrl = "url2";
        CachedAttachment cachedAttachment = Mockito.mock(CachedAttachment.class);
        when(cachedAttachment.getProxyUrl()).thenReturn(attachmentUrl);
        CachedAttachment secondCachedAttachment = Mockito.mock(CachedAttachment.class);
        when(secondCachedAttachment.getProxyUrl()).thenReturn(secondAttachmentUrl);
        List<CachedAttachment> cachedAttachments = Arrays.asList(cachedAttachment, secondCachedAttachment);
        when(deletedMessage.getAttachments()).thenReturn(cachedAttachments);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(member.getGuild()).thenReturn(guild);
        MessageToSend attachmentMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_TEMPLATE), captor.capture(), eq(SERVER_ID))).thenReturn(messageToSend);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_ATTACHMENT_TEMPLATE), attachmentCaptor.capture(), eq(SERVER_ID))).thenReturn(attachmentMessage);
        testUnit.executeListener(deletedMessage, member);
        verify(postTargetService, times(3)).sendEmbedInPostTarget(messageCaptor.capture(), eq(LoggingPostTarget.DELETE_LOG), eq(SERVER_ID));
        List<MessageToSend> messagesSent = messageCaptor.getAllValues();
        Assert.assertEquals(messageToSend, messagesSent.get(0));
        Assert.assertEquals(attachmentMessage, messagesSent.get(1));
        List<MessageDeletedAttachmentLog> attachmentLog = attachmentCaptor.getAllValues();
        verifyAttachmentLog(attachmentUrl, attachmentLog.get(0), 1);
        verifyAttachmentLog(secondAttachmentUrl, attachmentLog.get(1), 2);
        verifyMessageDeletedLog();
    }

    private void verifyMessageDeletedLog() {
        MessageDeletedLog messageDeletedLog = captor.getValue();
        Assert.assertEquals(guild, messageDeletedLog.getGuild());
        Assert.assertEquals(textChannel, messageDeletedLog.getChannel());
        Assert.assertEquals(member, messageDeletedLog.getMember());
    }

    private void verifyAttachmentLog(String attachmentUrl, MessageDeletedAttachmentLog attachmentLog, Integer index) {
        Assert.assertEquals(attachmentUrl, attachmentLog.getImageUrl());
        Assert.assertEquals(index, attachmentLog.getCounter());
        Assert.assertEquals(guild, attachmentLog.getGuild());
        Assert.assertEquals(textChannel, attachmentLog.getChannel());
        Assert.assertEquals(member, attachmentLog.getMember());
    }


}
