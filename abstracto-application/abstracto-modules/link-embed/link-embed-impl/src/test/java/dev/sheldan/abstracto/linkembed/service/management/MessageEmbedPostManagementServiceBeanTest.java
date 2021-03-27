package dev.sheldan.abstracto.linkembed.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.linkembed.exception.CrossServerEmbedException;
import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import dev.sheldan.abstracto.linkembed.repository.EmbeddedMessageRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageEmbedPostManagementServiceBeanTest {

    @InjectMocks
    private MessageEmbedPostManagementServiceBean testUnit;

    @Mock
    private EmbeddedMessageRepository embeddedMessageRepository;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Captor
    private ArgumentCaptor<EmbeddedMessage> messageArgumentCaptor;

    private static final Long SERVER_ID = 1L;
    private static final Long EMBEDDING_CHANNEL_ID = 2L;
    private static final Long EMBEDDED_MESSAGE_ID = 5L;
    private static final Long EMBEDDING_MESSAGE_ID = 7L;
    private static final Long EMBEDDED_USER_ID = 8L;
    private static final Long EMBEDDING_USER_ID = 9L;

    @Test
    public void testCreateCorrectEmbed() {
        AUserInAServer embeddingUser = Mockito.mock(AUserInAServer.class);
        AUserInAServer embeddedUser = Mockito.mock(AUserInAServer.class);
        AChannel channel = Mockito.mock(AChannel.class);
        AServer server = Mockito.mock(AServer.class);
        when(serverManagementService.loadOrCreate(SERVER_ID)).thenReturn(server);
        when(channelManagementService.loadChannel(EMBEDDING_CHANNEL_ID)).thenReturn(channel);
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(cachedMessage.getMessageId()).thenReturn(EMBEDDED_MESSAGE_ID);
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getChannelId()).thenReturn(EMBEDDING_CHANNEL_ID);
        CachedAuthor cachedAuthor = Mockito.mock(CachedAuthor.class);
        when(cachedAuthor.getAuthorId()).thenReturn(EMBEDDED_USER_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        Message embeddingMessage = Mockito.mock(Message.class);
        MessageChannel embeddingChannel = Mockito.mock(MessageChannel.class);
        when(embeddingChannel.getIdLong()).thenReturn(EMBEDDING_CHANNEL_ID);
        when(embeddingMessage.getChannel()).thenReturn(embeddingChannel);
        User embeddingJdaUser = Mockito.mock(User.class);
        when(embeddingJdaUser.getIdLong()).thenReturn(EMBEDDING_USER_ID);
        when(embeddingMessage.getAuthor()).thenReturn(embeddingJdaUser);
        Guild guild = Mockito.mock(Guild.class);
        when(embeddingMessage.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(embeddingMessage.getIdLong()).thenReturn(EMBEDDING_MESSAGE_ID);
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, EMBEDDED_USER_ID)).thenReturn(embeddedUser);
        testUnit.createMessageEmbed(cachedMessage, embeddingMessage, embeddingUser);
        verify(embeddedMessageRepository, times(1)).save(messageArgumentCaptor.capture());
        EmbeddedMessage savedMessage = messageArgumentCaptor.getValue();
        Assert.assertEquals(EMBEDDED_MESSAGE_ID, savedMessage.getEmbeddedMessageId());
        Assert.assertEquals(channel, savedMessage.getEmbeddedChannel());
        Assert.assertEquals(channel, savedMessage.getEmbeddingChannel());
        Assert.assertEquals(embeddedUser, savedMessage.getEmbeddedUser());
        Assert.assertEquals(embeddingUser, savedMessage.getEmbeddingUser());
        Assert.assertEquals(server, savedMessage.getEmbeddedServer());
        Assert.assertEquals(server, savedMessage.getEmbeddingServer());
        Assert.assertEquals(EMBEDDING_MESSAGE_ID, savedMessage.getEmbeddingMessageId());
    }

    @Test(expected = CrossServerEmbedException.class)
    public void testToCreateEmbedForDifferentServers() {
        Long originServerId = 4L;
        AServer originalServer = Mockito.mock(AServer.class);
        AServer embeddingServer = Mockito.mock(AServer.class);
        when(embeddingServer.getId()).thenReturn(SERVER_ID);
        AUserInAServer embeddingUser = Mockito.mock(AUserInAServer.class);
        when(serverManagementService.loadOrCreate(SERVER_ID)).thenReturn(embeddingServer);
        when(serverManagementService.loadOrCreate(originServerId)).thenReturn(originalServer);
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(cachedMessage.getServerId()).thenReturn(originServerId);
        Message message = Mockito.mock(Message.class);
        Guild guild = Mockito.mock(Guild.class);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        testUnit.createMessageEmbed(cachedMessage, message, embeddingUser);
    }

    @Test
    public void testDeleteEmbeddedMessage() {
        EmbeddedMessage message = Mockito.mock(EmbeddedMessage.class);
        testUnit.deleteEmbeddedMessage(message);
        verify(embeddedMessageRepository, times(1)).delete(message);
    }

    @Test
    public void testFindEmbeddedMessageByMessageIdSuccessful() {
        Long id = 5L;
        EmbeddedMessage message = Mockito.mock(EmbeddedMessage.class);
        when(embeddedMessageRepository.findByEmbeddingMessageId(id)).thenReturn(message);
        Optional<EmbeddedMessage> embeddedPostByMessageId = testUnit.findEmbeddedPostByMessageId(id);
        Assert.assertTrue(embeddedPostByMessageId.isPresent());
    }

    @Test
    public void testFindEmbeddedMessageByMessageIdFailing() {
        Long id = 5L;
        when(embeddedMessageRepository.findByEmbeddingMessageId(id)).thenReturn(null);
        Optional<EmbeddedMessage> embeddedPostByMessageId = testUnit.findEmbeddedPostByMessageId(id);
        Assert.assertFalse(embeddedPostByMessageId.isPresent());
    }

}
