package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.management.ModMailMessageManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModMailMessageDeletedListenerTest {

    @InjectMocks
    private ModMailMessageDeletedListener testUnit;

    @Mock
    private ModMailMessageManagementService modMailMessageManagementService;

    @Mock
    private MessageService messageService;

    @Mock
    private ModMailMessageDeletedListener self;

    @Mock
    private BotService botService;

    @Mock
    private CachedMessage deletedMessage;

    @Mock
    private AServerAChannelAUser origin;

    @Mock
    private GuildChannelMember jdaOrigin;

    @Mock
    private ModMailMessage modMailMessage;

    @Mock
    private Member targetMember;

    @Mock
    private User targetUser;

    @Mock
    private AServer server;

    @Mock
    private AChannel channel;

    private static final Long DELETED_MESSAGE_ID = 4L;
    private static final Long CREATED_MESSAGE_ID_1 = 3L;
    private static final Long CREATED_MESSAGE_ID_2 = 5L;
    private static final Long USER_ID = 5L;
    private static final Long SERVER_ID = 6L;
    private static final Long CHANNEL_ID = 9L;

    @Test
    public void testDeleteOutSideOfThread() {
        when(deletedMessage.getMessageId()).thenReturn(DELETED_MESSAGE_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(DELETED_MESSAGE_ID)).thenReturn(Optional.empty());
        testUnit.execute(deletedMessage, origin, jdaOrigin);
        verify(botService, times(0)).getMemberInServerAsync(anyLong(), anyLong());
    }

    @Test
    public void testDeleteNonDuplicatedMessage() {
        when(deletedMessage.getMessageId()).thenReturn(DELETED_MESSAGE_ID);
        when(deletedMessage.getServerId()).thenReturn(SERVER_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(DELETED_MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        AUserInAServer targetUsInAServer = Mockito.mock(AUserInAServer.class);
        when(thread.getUser()).thenReturn(targetUsInAServer);
        when(thread.getChannel()).thenReturn(channel);
        when(thread.getServer()).thenReturn(server);
        AUser targetAUser = Mockito.mock(AUser.class);
        when(targetUsInAServer.getUserReference()).thenReturn(targetAUser);
        when(modMailMessage.getCreatedMessageInChannel()).thenReturn(null);
        when(modMailMessage.getCreatedMessageInDM()).thenReturn(CREATED_MESSAGE_ID_2);
        when(targetAUser.getId()).thenReturn(USER_ID);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        when(targetMember.getUser()).thenReturn(targetUser);
        when(botService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(targetMember));
        when(messageService.deleteMessageInChannelWithUser(targetUser, CREATED_MESSAGE_ID_2)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.execute(deletedMessage, origin, jdaOrigin);
        verify(messageService, times(0)).deleteMessageInChannelInServer(eq(SERVER_ID), anyLong(), any());
        verify(self, times(1)).removeMessageFromThread(DELETED_MESSAGE_ID);
    }

    @Test
    public void testDeleteDuplicatedMessage() {
        when(deletedMessage.getMessageId()).thenReturn(DELETED_MESSAGE_ID);
        when(deletedMessage.getServerId()).thenReturn(SERVER_ID);
        when(modMailMessageManagementService.getByMessageIdOptional(DELETED_MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        ModMailThread thread = Mockito.mock(ModMailThread.class);
        AUserInAServer targetUsInAServer = Mockito.mock(AUserInAServer.class);
        when(thread.getUser()).thenReturn(targetUsInAServer);
        when(thread.getChannel()).thenReturn(channel);
        when(thread.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(channel.getId()).thenReturn(CHANNEL_ID);
        AUser targetAUser = Mockito.mock(AUser.class);
        when(targetUsInAServer.getUserReference()).thenReturn(targetAUser);
        when(modMailMessage.getCreatedMessageInChannel()).thenReturn(CREATED_MESSAGE_ID_1);
        when(modMailMessage.getCreatedMessageInDM()).thenReturn(CREATED_MESSAGE_ID_2);
        when(targetAUser.getId()).thenReturn(USER_ID);
        when(modMailMessage.getThreadReference()).thenReturn(thread);
        when(targetMember.getUser()).thenReturn(targetUser);
        when(botService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(targetMember));
        when(messageService.deleteMessageInChannelWithUser(targetUser, CREATED_MESSAGE_ID_2)).thenReturn(CompletableFuture.completedFuture(null));
        when(messageService.deleteMessageInChannelInServer(SERVER_ID, CHANNEL_ID, CREATED_MESSAGE_ID_1)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.execute(deletedMessage, origin, jdaOrigin);
        verify(self, times(1)).removeMessageFromThread(DELETED_MESSAGE_ID);
    }

    @Test
    public void removeMessageFromThread() {
        when(modMailMessageManagementService.getByMessageIdOptional(DELETED_MESSAGE_ID)).thenReturn(Optional.of(modMailMessage));
        testUnit.removeMessageFromThread(DELETED_MESSAGE_ID);
        verify(modMailMessageManagementService, times(1)).deleteMessageFromThread(modMailMessage);
    }
}