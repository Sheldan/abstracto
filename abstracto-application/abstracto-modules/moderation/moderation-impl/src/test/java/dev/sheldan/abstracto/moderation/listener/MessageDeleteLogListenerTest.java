package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedAttachmentLog;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageDeletedLog;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageDeleteLogListenerTest {
    @InjectMocks
    private MessageDeleteLogListener testUnit;

    @Mock
    private ContextUtils contextUtils;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    private AServerAChannelAUser authorUser;
    private GuildChannelMember authorMember;

    @Captor
    private ArgumentCaptor<MessageDeletedLog> captor;

    @Captor
    private ArgumentCaptor<MessageDeletedAttachmentLog> attachmentCaptor;

    @Captor
    private ArgumentCaptor<MessageToSend> messageCaptor;

    @Before
    public void setup() {
        AServer server = MockUtils.getServer();
        AUserInAServer aUserInAServer = MockUtils.getUserObject(4L, server);
        AChannel channel = MockUtils.getTextChannel(server, 5L);
        authorUser = AServerAChannelAUser.builder().guild(server).channel(channel).aUserInAServer(aUserInAServer).build();
        Member member = Mockito.mock(Member.class);
        Guild guild = Mockito.mock(Guild.class);
        TextChannel textChannel = Mockito.mock(TextChannel.class);
        authorMember = GuildChannelMember.builder().guild(guild).textChannel(textChannel).member(member).build();
    }

    @Test
    public void testExecuteListenerWithSimpleMessage() {
        CachedMessage message = CachedMessage.builder().serverId(authorUser.getGuild().getId()).build();
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_TEMPLATE), captor.capture())).thenReturn(messageToSend);
        testUnit.execute(message, authorUser, authorMember);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, LoggingPostTarget.DELETE_LOG, authorUser.getGuild().getId());
        MessageDeletedLog messageDeletedLog = captor.getValue();
        Assert.assertEquals(message, messageDeletedLog.getCachedMessage());
        Assert.assertEquals(authorUser.getGuild(), messageDeletedLog.getServer());
        Assert.assertEquals(authorUser.getChannel(), messageDeletedLog.getChannel());
        Assert.assertEquals(authorUser.getUser(), messageDeletedLog.getUser());
        Assert.assertEquals(authorUser.getAUserInAServer(), messageDeletedLog.getAUserInAServer());
        Assert.assertEquals(authorMember.getGuild(), messageDeletedLog.getGuild());
        Assert.assertEquals(authorMember.getTextChannel(), messageDeletedLog.getMessageChannel());
        Assert.assertEquals(authorMember.getMember(), messageDeletedLog.getMember());
    }

    @Test
    public void testExecuteListenerWithOneAttachment() {
        String attachmentUrl = "url";
        CachedMessage message = CachedMessage.builder().serverId(authorUser.getGuild().getId()).attachmentUrls(Arrays.asList(attachmentUrl)).build();
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        MessageToSend attachmentMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_TEMPLATE), captor.capture())).thenReturn(messageToSend);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_ATTACHMENT_TEMPLATE), attachmentCaptor.capture())).thenReturn(attachmentMessage);
        testUnit.execute(message, authorUser, authorMember);
        verify(postTargetService, times(2)).sendEmbedInPostTarget(messageCaptor.capture(), eq(LoggingPostTarget.DELETE_LOG), eq(authorUser.getGuild().getId()));
        List<MessageToSend> messagesSent = messageCaptor.getAllValues();
        Assert.assertEquals(messageToSend, messagesSent.get(0));
        Assert.assertEquals(attachmentMessage, messagesSent.get(1));
        MessageDeletedAttachmentLog attachmentLog = attachmentCaptor.getValue();
        verifyAttachmentLog(attachmentUrl, attachmentLog, 1);
        verifyMessageDeletedLog();
    }

    @Test
    public void testExecuteListenerWithTwoAttachment() {
        String attachmentUrl = "url";
        String secondAttachmentUrl = "url2";
        CachedMessage message = CachedMessage.builder().serverId(authorUser.getGuild().getId()).attachmentUrls(Arrays.asList(attachmentUrl, secondAttachmentUrl)).build();
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        MessageToSend attachmentMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_TEMPLATE), captor.capture())).thenReturn(messageToSend);
        when(templateService.renderEmbedTemplate(eq(MessageDeleteLogListener.MESSAGE_DELETED_ATTACHMENT_TEMPLATE), attachmentCaptor.capture())).thenReturn(attachmentMessage);
        testUnit.execute(message, authorUser, authorMember);
        verify(postTargetService, times(3)).sendEmbedInPostTarget(messageCaptor.capture(), eq(LoggingPostTarget.DELETE_LOG), eq(authorUser.getGuild().getId()));
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
        Assert.assertEquals(authorUser.getGuild(), messageDeletedLog.getServer());
        Assert.assertEquals(authorUser.getChannel(), messageDeletedLog.getChannel());
        Assert.assertEquals(authorUser.getUser(), messageDeletedLog.getUser());
        Assert.assertEquals(authorUser.getAUserInAServer(), messageDeletedLog.getAUserInAServer());
        Assert.assertEquals(authorMember.getGuild(), messageDeletedLog.getGuild());
        Assert.assertEquals(authorMember.getTextChannel(), messageDeletedLog.getMessageChannel());
        Assert.assertEquals(authorMember.getMember(), messageDeletedLog.getMember());
    }

    private void verifyAttachmentLog(String attachmentUrl, MessageDeletedAttachmentLog attachmentLog, Integer index) {
        Assert.assertEquals(attachmentUrl, attachmentLog.getImageUrl());
        Assert.assertEquals(index, attachmentLog.getCounter());
        Assert.assertEquals(authorUser.getGuild(), attachmentLog.getServer());
        Assert.assertEquals(authorUser.getChannel(), attachmentLog.getChannel());
        Assert.assertEquals(authorUser.getUser(), attachmentLog.getUser());
        Assert.assertEquals(authorUser.getAUserInAServer(), attachmentLog.getAUserInAServer());
        Assert.assertEquals(authorMember.getGuild(), attachmentLog.getGuild());
        Assert.assertEquals(authorMember.getTextChannel(), attachmentLog.getMessageChannel());
        Assert.assertEquals(authorMember.getMember(), attachmentLog.getMember());
    }


}
