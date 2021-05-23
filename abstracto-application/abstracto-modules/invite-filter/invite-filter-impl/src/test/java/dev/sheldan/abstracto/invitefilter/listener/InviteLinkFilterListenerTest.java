package dev.sheldan.abstracto.invitefilter.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterServiceBean;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InviteLinkFilterListenerTest {

    @InjectMocks
    private InviteLinkFilterListener testUnit;

    @Mock
    private InviteLinkFilterService inviteLinkFilterService;

    @Mock
    private Message message;

    @Mock
    private Member member;

    @Mock
    private MessageChannel messageChannel;

    @Mock
    private InviteLinkFilterServiceBean filterServiceBean;

    @Mock
    private MessageReceivedModel model;

    private static final String INVITE_CODE = "code";

    @Test
    public void testExecutionWithNoInvite() {
        when(inviteLinkFilterService.findInvitesInMessage(message)).thenReturn(new ArrayList<>());
        setupBasicMessage();
        when(model.getMessage()).thenReturn(message);
        DefaultListenerResult result = testUnit.execute(model);
        Assert.assertEquals(DefaultListenerResult.IGNORED, result);
        verify(filterServiceBean, times(0)).resolveAndCheckInvites(eq(message), any());
    }

    @Test
    public void testExecutionWithOneInvite() {
        when(inviteLinkFilterService.findInvitesInMessage(message)).thenReturn(Arrays.asList(INVITE_CODE));
        setupBasicMessage();
        when(model.getMessage()).thenReturn(message);
        DefaultListenerResult result = testUnit.execute(model);
        Assert.assertEquals(DefaultListenerResult.PROCESSED, result);
        verify(filterServiceBean, times(1)).resolveAndCheckInvites(eq(message), any());
    }

    private void setupBasicMessage() {
        when(message.getChannel()).thenReturn(messageChannel);
        when(message.getMember()).thenReturn(member);
        when(message.isFromGuild()).thenReturn(true);
        when(message.isWebhookMessage()).thenReturn(false);
        when(inviteLinkFilterService.isInviteFilterActiveInChannel(messageChannel)).thenReturn(true);
        when(inviteLinkFilterService.isMemberImmuneAgainstInviteFilter(member)).thenReturn(false);
        MessageType type = MessageType.DEFAULT;
        when(message.getType()).thenReturn(type);
    }
}
