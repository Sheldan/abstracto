package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.service.CommandRegistry;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.modmail.model.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.template.ModMailModeratorReplyModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.ModMailThreadServiceBean;
import dev.sheldan.abstracto.modmail.service.management.ModMailMessageManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.modmail.listener.ModMailMessageEditedListener.DEFAULT_COMMAND_FOR_MODMAIL_EDIT;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModMailMessageEditedListenerTest {

    @InjectMocks
    private ModMailMessageEditedListener testUnit;

    @Mock
    private ModMailThreadService modMailThreadService;

    @Mock
    private ModMailMessageManagementService modMailMessageManagementService;

    @Mock
    private CommandRegistry commandRegistry;

    @Mock
    private CommandService commandService;

    @Mock
    private MemberService memberService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ChannelService channelService;

    @Mock
    private MessageService messageService;

    @Mock
    private ModMailMessageEditedListener self;

    @Mock
    private CachedMessage messageBefore;

    @Mock
    private CachedMessage messageAfter;

    @Mock
    private Message loadedMessage;

    @Mock
    private ModMailMessage modMailMessage;

    @Mock
    private Parameters parsedParameters;

    @Mock
    private Member targetMember;

    @Mock
    private User targetUser;

    @Mock
    private MessageToSend messageToSend;

    @Mock
    private Member authorMember;

    @Mock
    private Guild guild;

    @Captor
    private ArgumentCaptor<ModMailModeratorReplyModel> replyModelArgumentCaptor;

    private static final Long CHANNEL_ID = 5L;
    private static final Long MESSAGE_ID = 6L;
    private static final Long CREATED_MESSAGE_ID = 10L;
    private static final String NEW_COMMAND_PART = "editedText";
    private static final String NEW_PARAM = "param";
    private static final String NEW_CONTENT = NEW_COMMAND_PART + " " + NEW_PARAM;
    private static final Long SERVER_ID = 4L;
    private static final Long USER_ID = 3L;
    private static final Long AUTHOR_USER_ID = 9L;

    @Test
    public void testMessageLoading() {
        when(messageBefore.getChannelId()).thenReturn(CHANNEL_ID);
        when(modMailThreadService.isModMailThread(CHANNEL_ID)).thenReturn(true);
        when(messageService.loadMessageFromCachedMessage(messageAfter)).thenReturn(CompletableFuture.completedFuture(loadedMessage));
        testUnit.execute(messageBefore, messageAfter);
        verify(self, times(1)).executeMessageUpdatedLogic(messageBefore, messageAfter, loadedMessage);
    }

    @Test
    public void testEditOutsideModMailThread() {
        when(modMailThreadService.isModMailThread(CHANNEL_ID)).thenReturn(false);
        when(messageBefore.getChannelId()).thenReturn(CHANNEL_ID);
        testUnit.execute(messageBefore, messageAfter);
        verify(modMailMessageManagementService, times(0)).getByMessageIdOptional(anyLong());
    }

    @Test
    public void testEditNotTrackedMessage() {
        when(messageBefore.getMessageId()).thenReturn(MESSAGE_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(MESSAGE_ID)).thenReturn(Optional.empty());
        testUnit.executeMessageUpdatedLogic(messageBefore, messageAfter, loadedMessage);
        verify(commandRegistry, times(0)).getCommandName(anyString(), anyLong());
    }

    @Test
    public void testEditMessageWithCorrectCommand() {
        when(messageBefore.getChannelId()).thenReturn(CHANNEL_ID);
        when(messageBefore.getMessageId()).thenReturn(MESSAGE_ID);
        when(messageBefore.getServerId()).thenReturn(SERVER_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        AUserInAServer targetUsInAServer = Mockito.mock(AUserInAServer.class);
        when(thread.getUser()).thenReturn(targetUsInAServer);
        AUser targetUser = Mockito.mock(AUser.class);
        when(targetUsInAServer.getUserReference()).thenReturn(targetUser);
        when(targetUser.getId()).thenReturn(USER_ID);
        AUserInAServer authorUserInAServer = Mockito.mock(AUserInAServer.class);
        when(modMailMessage.getAuthor()).thenReturn(authorUserInAServer);
        AUser authorUser = Mockito.mock(AUser.class);
        when(authorUser.getId()).thenReturn(AUTHOR_USER_ID);
        when(authorUserInAServer.getUserReference()).thenReturn(authorUser);
        when(messageAfter.getContent()).thenReturn(NEW_CONTENT);
        when(commandRegistry.getCommandName(NEW_COMMAND_PART, SERVER_ID)).thenReturn(NEW_COMMAND_PART);
        when(commandService.doesCommandExist(NEW_COMMAND_PART)).thenReturn(true);
        when(commandService.getParametersForCommand(NEW_COMMAND_PART, loadedMessage)).thenReturn(CompletableFuture.completedFuture(parsedParameters));
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(targetMember));
        when(memberService.getMemberInServerAsync(SERVER_ID, AUTHOR_USER_ID)).thenReturn(CompletableFuture.completedFuture(authorMember));
        testUnit.executeMessageUpdatedLogic(messageBefore, messageAfter, loadedMessage);
        verify(self, times(1)).updateMessageInThread(loadedMessage, parsedParameters, targetMember, authorMember);
    }

    @Test
    public void testEditMessageWithInCorrectCommand() {
        when(messageBefore.getChannelId()).thenReturn(CHANNEL_ID);
        when(messageBefore.getMessageId()).thenReturn(MESSAGE_ID);
        when(messageBefore.getServerId()).thenReturn(SERVER_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        AUserInAServer aUserInAServer = Mockito.mock(AUserInAServer.class);
        when(thread.getUser()).thenReturn(aUserInAServer);
        AUser user = Mockito.mock(AUser.class);
        when(aUserInAServer.getUserReference()).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);
        AUserInAServer authorUserInAServer = Mockito.mock(AUserInAServer.class);
        when(modMailMessage.getAuthor()).thenReturn(authorUserInAServer);
        AUser authorUser = Mockito.mock(AUser.class);
        when(authorUser.getId()).thenReturn(AUTHOR_USER_ID);
        when(authorUserInAServer.getUserReference()).thenReturn(authorUser);
        when(messageAfter.getContent()).thenReturn(NEW_CONTENT);
        when(commandRegistry.getCommandName(NEW_COMMAND_PART, SERVER_ID)).thenReturn(NEW_COMMAND_PART);
        when(commandService.doesCommandExist(NEW_COMMAND_PART)).thenReturn(false);
        when(commandService.getParametersForCommand(DEFAULT_COMMAND_FOR_MODMAIL_EDIT, loadedMessage)).thenReturn(CompletableFuture.completedFuture(parsedParameters));
        when(memberService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(targetMember));
        when(memberService.getMemberInServerAsync(SERVER_ID, AUTHOR_USER_ID)).thenReturn(CompletableFuture.completedFuture(authorMember));
        testUnit.executeMessageUpdatedLogic(messageBefore, messageAfter, loadedMessage);
        verify(self, times(1)).updateMessageInThread(loadedMessage, parsedParameters, targetMember, authorMember);
    }

    @Test
    public void testUpdateAnonymousMessageInThreadNotSentToModMailThreadChannel() {
        when(loadedMessage.getIdLong()).thenReturn(MESSAGE_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        when(modMailMessage.getAnonymous()).thenReturn(true);
        when(modMailMessage.getCreatedMessageInChannel()).thenReturn(null);
        when(modMailMessage.getCreatedMessageInDM()).thenReturn(CREATED_MESSAGE_ID);
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        when(targetMember.getUser()).thenReturn(targetUser);
        when(authorMember.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(parsedParameters.getParameters()).thenReturn(Arrays.asList(NEW_PARAM));
        when(templateService.renderEmbedTemplate(eq(ModMailThreadServiceBean.MODMAIL_STAFF_MESSAGE_TEMPLATE_KEY), replyModelArgumentCaptor.capture())).thenReturn(messageToSend);
        testUnit.updateMessageInThread(loadedMessage, parsedParameters, targetMember, authorMember);
        verify(channelService, times(0)).editMessageInAChannel(eq(messageToSend), any(AChannel.class), anyLong());
        verify(messageService, times(1)).editMessageInDMChannel(targetUser, messageToSend, CREATED_MESSAGE_ID);
        Assert.assertTrue(replyModelArgumentCaptor.getValue().getAnonymous());
    }

    @Test
    public void testUpdateAnonymousMessageInThreadAlsoSendToModMailThreadChannel() {
        when(loadedMessage.getIdLong()).thenReturn(MESSAGE_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        when(modMailMessage.getAnonymous()).thenReturn(true);
        when(modMailMessage.getCreatedMessageInChannel()).thenReturn(CREATED_MESSAGE_ID);
        when(modMailMessage.getCreatedMessageInDM()).thenReturn(CREATED_MESSAGE_ID);
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        when(targetMember.getUser()).thenReturn(targetUser);
        when(authorMember.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        AChannel channel = Mockito.mock(AChannel.class);
        when(thread.getChannel()).thenReturn(channel);
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(parsedParameters.getParameters()).thenReturn(Arrays.asList(NEW_PARAM));
        when(templateService.renderEmbedTemplate(eq(ModMailThreadServiceBean.MODMAIL_STAFF_MESSAGE_TEMPLATE_KEY), replyModelArgumentCaptor.capture())).thenReturn(messageToSend);
        testUnit.updateMessageInThread(loadedMessage, parsedParameters, targetMember, authorMember);
        verify(channelService, times(1)).editMessageInAChannel(eq(messageToSend), eq(channel), eq(CREATED_MESSAGE_ID));
        verify(messageService, times(1)).editMessageInDMChannel(targetUser, messageToSend, CREATED_MESSAGE_ID);
        Assert.assertTrue(replyModelArgumentCaptor.getValue().getAnonymous());
    }

    @Test
    public void testUpdateMessageInThreadNotDuplicated() {
        when(loadedMessage.getIdLong()).thenReturn(MESSAGE_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        when(modMailMessage.getAnonymous()).thenReturn(false);
        when(modMailMessage.getCreatedMessageInChannel()).thenReturn(null);
        when(modMailMessage.getCreatedMessageInDM()).thenReturn(CREATED_MESSAGE_ID);
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        when(targetMember.getUser()).thenReturn(targetUser);
        when(authorMember.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(parsedParameters.getParameters()).thenReturn(Arrays.asList(NEW_PARAM));
        when(templateService.renderEmbedTemplate(eq(ModMailThreadServiceBean.MODMAIL_STAFF_MESSAGE_TEMPLATE_KEY), replyModelArgumentCaptor.capture())).thenReturn(messageToSend);
        testUnit.updateMessageInThread(loadedMessage, parsedParameters, targetMember, authorMember);
        verify(channelService, times(0)).editMessageInAChannel(eq(messageToSend), any(AChannel.class), anyLong());
        verify(messageService, times(1)).editMessageInDMChannel(targetUser, messageToSend, CREATED_MESSAGE_ID);
        Assert.assertFalse(replyModelArgumentCaptor.getValue().getAnonymous());
    }

    @Test
    public void testUpdateMessageInThreadDuplicated() {
        when(loadedMessage.getIdLong()).thenReturn(MESSAGE_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        when(modMailMessage.getAnonymous()).thenReturn(false);
        when(modMailMessage.getCreatedMessageInChannel()).thenReturn(CREATED_MESSAGE_ID);
        when(modMailMessage.getCreatedMessageInDM()).thenReturn(CREATED_MESSAGE_ID);
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        when(targetMember.getUser()).thenReturn(targetUser);
        when(authorMember.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        AChannel channel = Mockito.mock(AChannel.class);
        when(thread.getChannel()).thenReturn(channel);
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(parsedParameters.getParameters()).thenReturn(Arrays.asList(NEW_PARAM));
        when(templateService.renderEmbedTemplate(eq(ModMailThreadServiceBean.MODMAIL_STAFF_MESSAGE_TEMPLATE_KEY), replyModelArgumentCaptor.capture())).thenReturn(messageToSend);
        testUnit.updateMessageInThread(loadedMessage, parsedParameters, targetMember, authorMember);
        verify(channelService, times(1)).editMessageInAChannel(eq(messageToSend), eq(channel), eq(CREATED_MESSAGE_ID));
        verify(messageService, times(1)).editMessageInDMChannel(targetUser, messageToSend, CREATED_MESSAGE_ID);
        Assert.assertFalse(replyModelArgumentCaptor.getValue().getAnonymous());
    }
}