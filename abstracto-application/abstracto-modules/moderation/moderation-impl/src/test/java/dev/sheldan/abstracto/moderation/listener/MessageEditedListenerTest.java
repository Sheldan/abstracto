package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageTextUpdatedModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttarget.LoggingPostTarget;
import dev.sheldan.abstracto.moderation.model.template.listener.MessageEditedLog;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageEditedListenerTest {

    @InjectMocks
    private MessageEditedListener testUnit;

    @Mock
    private TemplateService templateService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private ChannelService channelService;

    @Mock
    private MemberService memberService;

    @Mock
    private Message messageAfter;

    @Mock
    private CachedMessage messageBefore;

    private MessageTextUpdatedModel model;

    private static final Long SERVER_ID = 4L;
    private static final Long CHANNEL_ID = 5L;
    private static final Long AUTHOR_ID = 6L;

    @Test
    public void testExecuteListenerWithSameContent() {
        String content = "text";
        when(messageAfter.getContentRaw()).thenReturn(content);
        when(messageBefore.getContent()).thenReturn(content);
        when(model.getAfter()).thenReturn(messageAfter);
        when(model.getBefore()).thenReturn(messageBefore);
        testUnit.execute(model);
        verify(templateService, times(0)).renderEmbedTemplate(eq(MessageEditedListener.MESSAGE_EDITED_TEMPLATE), any());
    }

    @Test
    public void testExecuteListenerWithDifferentContent() {
        String content = "text";
        String contentAfterwards = "text2";
        TextChannel channel = Mockito.mock(TextChannel.class);
        when(messageAfter.getContentRaw()).thenReturn(contentAfterwards);
        Guild guild = Mockito.mock(Guild.class);
        when(channel.getGuild()).thenReturn(guild);
        Member author = Mockito.mock(Member.class);
        CachedAuthor cachedAuthor = Mockito.mock(CachedAuthor.class);
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(messageBefore.getContent()).thenReturn(content);
        when(messageBefore.getServerId()).thenReturn(SERVER_ID);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        ArgumentCaptor<MessageEditedLog> captor = ArgumentCaptor.forClass(MessageEditedLog.class);
        when(templateService.renderEmbedTemplate(eq(MessageEditedListener.MESSAGE_EDITED_TEMPLATE), captor.capture(), eq(SERVER_ID))).thenReturn(messageToSend);
        when(memberService.getMemberInServerAsync(SERVER_ID, AUTHOR_ID)).thenReturn(CompletableFuture.completedFuture(author));
        when(channelService.getTextChannelFromServer(SERVER_ID, CHANNEL_ID)).thenReturn(channel);
        when(model.getAfter()).thenReturn(messageAfter);
        when(model.getBefore()).thenReturn(messageBefore);
        testUnit.execute(model);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, LoggingPostTarget.EDIT_LOG, SERVER_ID);
        MessageEditedLog capturedValue = captor.getValue();
        Assert.assertEquals(messageBefore, capturedValue.getMessageBefore());
        Assert.assertEquals(messageAfter, capturedValue.getMessageAfter());
        Assert.assertEquals(channel, capturedValue.getMessageChannel());
        Assert.assertEquals(guild, capturedValue.getGuild());
        Assert.assertEquals(author, capturedValue.getMember());
    }
}