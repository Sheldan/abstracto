package dev.sheldan.abstracto.invitefilter.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.RoleImmunityService;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService.INVITE_FILTER_CHANNEL_GROUP_TYPE;
import static dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService.INVITE_FILTER_EFFECT_KEY;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InviteLinkFilterListenerTest {

    @InjectMocks
    private InviteLinkFilterListener testUnit;

    @Mock
    private InviteLinkFilterService inviteLinkFilterService;

    @Mock
    private Message message;

    @Mock
    private User author;

    @Mock
    private Member member;

    @Mock
    private MessageChannel messageChannel;

    @Mock
    private Guild guild;

    @Mock
    private MessageReceivedModel model;

    @Mock
    private ChannelGroupService channelGroupService;

    @Mock
    private RoleImmunityService roleImmunityService;

    @Mock
    private JDA jda;

    @Mock
    private Invite invite;

    private static final Long SERVER_ID = 1L;
    private static final Long CHANNEL_ID = 2L;
    private static final Long USER_ID = 3L;
    private static final String INVITE_CODE = "code";
    private static final String INVITE_LINK = "discord.gg/" + INVITE_CODE;

    @Test
    public void testExecutionWithNoInvite() {
        when(message.getContentRaw()).thenReturn("text");
        setupBasicMessage();
        setupFiltering();
        when(model.getMessage()).thenReturn(message);
        DefaultListenerResult result = testUnit.execute(model);
        Assert.assertEquals(DefaultListenerResult.IGNORED, result);
    }

    @Test
    public void testExecutionWithOneInvite() {
        setupFiltering();
        when(message.getContentRaw()).thenReturn(INVITE_LINK);
        setupBasicMessage();
        when(inviteLinkFilterService.resolveInvite(jda, INVITE_CODE)).thenReturn(CompletableFuture.completedFuture(invite));
        when(model.getMessage()).thenReturn(message);
        DefaultListenerResult result = testUnit.execute(model);
        Assert.assertEquals(DefaultListenerResult.PROCESSED, result);
    }

    private void setupFiltering() {
        when(channelGroupService.isChannelInEnabledChannelGroupOfType(INVITE_FILTER_CHANNEL_GROUP_TYPE, CHANNEL_ID)).thenReturn(true);
        when(roleImmunityService.isImmune(member, INVITE_FILTER_EFFECT_KEY)).thenReturn(false);
    }

    private void setupBasicMessage() {
        when(messageChannel.getIdLong()).thenReturn(CHANNEL_ID);
        when(message.getChannel()).thenReturn(messageChannel);
        when(message.getAuthor()).thenReturn(author);
        when(message.getMember()).thenReturn(member);
        when(author.getIdLong()).thenReturn(USER_ID);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(message.isFromGuild()).thenReturn(true);
        when(message.isWebhookMessage()).thenReturn(false);
        when(message.getJDA()).thenReturn(jda);
        MessageType type = MessageType.DEFAULT;
        when(message.getType()).thenReturn(type);
    }
}
