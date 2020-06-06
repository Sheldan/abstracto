package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.exception.CrossServerEmbedException;
import dev.sheldan.abstracto.utility.models.database.EmbeddedMessage;
import dev.sheldan.abstracto.utility.repository.EmbeddedMessageRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
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

    @Test
    public void testCreateCorrectEmbed(){
        AServer server = MockUtils.getServer();
        AUserInAServer embeddingUser = MockUtils.getUserObject(5L, server);
        AUserInAServer embeddedUser = MockUtils.getUserObject(7L, server);
        AChannel channel = MockUtils.getTextChannel(server, 8L);
        when(serverManagementService.loadOrCreate(server.getId())).thenReturn(server);
        when(channelManagementService.loadChannel(channel.getId())).thenReturn(Optional.of(channel));
        Long embeddedMessageId = 5L;
        Long embeddingMessageId = 7L;
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .messageId(embeddedMessageId)
                .channelId(channel.getId())
                .serverId(server.getId())
                .authorId(embeddedUser.getUserReference().getId())
                .build();
        Message message = Mockito.mock(Message.class);
        Guild guild = Mockito.mock(Guild.class);
        MessageChannel embeddedChannel = Mockito.mock(MessageChannel.class);
        when(message.getChannel()).thenReturn(embeddedChannel);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(server.getId());
        when(message.getIdLong()).thenReturn(embeddingMessageId);
        when(embeddedChannel.getIdLong()).thenReturn(channel.getId());
        when(userInServerManagementService.loadUser(server.getId(), embeddedUser.getUserReference().getId())).thenReturn(embeddedUser);
        testUnit.createMessageEmbed(cachedMessage, message, embeddingUser);
        verify(embeddedMessageRepository, times(1)).save(messageArgumentCaptor.capture());
        EmbeddedMessage savedMessage = messageArgumentCaptor.getValue();
        Assert.assertEquals(embeddedMessageId, savedMessage.getEmbeddedMessageId());
        Assert.assertEquals(channel, savedMessage.getEmbeddedChannel());
        Assert.assertEquals(channel, savedMessage.getEmbeddingChannel());
        Assert.assertEquals(embeddedUser, savedMessage.getEmbeddedUser());
        Assert.assertEquals(embeddingUser, savedMessage.getEmbeddingUser());
        Assert.assertEquals(server, savedMessage.getEmbeddedServer());
        Assert.assertEquals(server, savedMessage.getEmbeddingServer());
        Assert.assertEquals(embeddingMessageId, savedMessage.getEmbeddingMessageId());
    }

    @Test(expected = CrossServerEmbedException.class)
    public void testToCreateEmbedForDifferentServers() {
        AServer originalServer = MockUtils.getServer(7L);
        AServer embeddingServer = MockUtils.getServer(9L);
        AUserInAServer embeddingUser = MockUtils.getUserObject(5L, embeddingServer);
        when(serverManagementService.loadOrCreate(embeddingServer.getId())).thenReturn(embeddingServer);
        when(serverManagementService.loadOrCreate(originalServer.getId())).thenReturn(originalServer);
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .serverId(originalServer.getId())
                .build();
        Message message = Mockito.mock(Message.class);
        Guild guild = Mockito.mock(Guild.class);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(embeddingServer.getId());
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
