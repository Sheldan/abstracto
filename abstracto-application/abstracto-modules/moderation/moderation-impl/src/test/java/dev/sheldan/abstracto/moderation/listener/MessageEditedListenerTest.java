package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttargets.LoggingPostTarget;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageEditedLog;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
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
    private Message messageAfter;

    @Mock
    private CachedMessage messageBefore;

    @Test
    public void testExecuteListenerWithSameContent() {
        String content = "text";
        when(messageAfter.getContentRaw()).thenReturn(content);
        when(messageBefore.getContent()).thenReturn(content);
        testUnit.execute(messageBefore, messageAfter);
        verify(templateService, times(0)).renderEmbedTemplate(eq(MessageEditedListener.MESSAGE_EDITED_TEMPLATE), any());
    }

    @Test
    public void testExecuteListenerWithDifferentContent() {
        String content = "text";
        String contentAfterwards = "text2";
        TextChannel channel = Mockito.mock(TextChannel.class);
        Long serverId = 5L;
        when(messageAfter.getContentRaw()).thenReturn(contentAfterwards);
        when(messageAfter.getTextChannel()).thenReturn(channel);
        Guild guild = Mockito.mock(Guild.class);
        when(messageAfter.getGuild()).thenReturn(guild);
        Member member = Mockito.mock(Member.class);
        when(messageAfter.getMember()).thenReturn(member);
        when(messageBefore.getContent()).thenReturn(content);
        when(messageBefore.getServerId()).thenReturn(serverId);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        ArgumentCaptor<MessageEditedLog> captor = ArgumentCaptor.forClass(MessageEditedLog.class);
        when(templateService.renderEmbedTemplate(eq(MessageEditedListener.MESSAGE_EDITED_TEMPLATE), captor.capture())).thenReturn(messageToSend);
        testUnit.execute(messageBefore, messageAfter);
        verify(postTargetService, times(1)).sendEmbedInPostTarget(messageToSend, LoggingPostTarget.EDIT_LOG, serverId);
        MessageEditedLog capturedValue = captor.getValue();
        Assert.assertEquals(messageBefore, capturedValue.getMessageBefore());
        Assert.assertEquals(messageAfter, capturedValue.getMessageAfter());
        Assert.assertEquals(channel, capturedValue.getMessageChannel());
        Assert.assertEquals(guild, capturedValue.getGuild());
        Assert.assertEquals(member, capturedValue.getMember());
    }
}
