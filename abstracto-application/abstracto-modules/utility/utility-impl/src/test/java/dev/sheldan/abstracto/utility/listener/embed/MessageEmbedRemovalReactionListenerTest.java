package dev.sheldan.abstracto.utility.listener.embed;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.models.database.EmbeddedMessage;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageEmbedRemovalReactionListenerTest {

    @InjectMocks
    private MessageEmbedRemovalReactionListener testUnit;

    @Mock
    private BotService botService;

    @Mock
    private MessageEmbedPostManagementService messageEmbedPostManagementService;

    @Mock
    private MessageService messageService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private GuildMessageReactionAddEvent messageReaction;

    @Mock
    private MessageReaction.ReactionEmote reactionEmote;

    @Mock
    private Emote emote;

    @Test
    public void testAddingWrongEmote() {
        Long messageId = 4L;
        executeRemovalAddedTest(false, messageId);
        verify(messageEmbedPostManagementService, times(0)).findEmbeddedPostByMessageId(messageId);
    }

    @Test
    public void testAddingCorrectEmoteToWrongMessage() {
        Long messageId = 4L;
        when(messageEmbedPostManagementService.findEmbeddedPostByMessageId(messageId)).thenReturn(Optional.empty());
        executeRemovalAddedTest(true, messageId);
    }

    @Test
    public void testIncorrectUserAddingReaction() {
        Long serverId = 4L;
        AServer server = MockUtils.getServer(serverId);
        AUserInAServer embeddingUser = MockUtils.getUserObject(6L, server);
        AUserInAServer embeddedUser = MockUtils.getUserObject(7L, server);
        executeDeletionTest(serverId, 5L, embeddingUser, embeddedUser,  MockUtils.getUserObject(5L, server), 0);
    }

    @Test
    public void testEmbeddedUserAddingReaction() {
        Long serverId = 4L;
        AServer server = MockUtils.getServer(serverId);
        AUserInAServer embeddingUser = MockUtils.getUserObject(6L, server);
        AUserInAServer embeddedUser = MockUtils.getUserObject(7L, server);
        executeDeletionTest(serverId, 4L, embeddingUser, embeddedUser, embeddedUser, 1);
    }

    @Test
    public void testEmbeddingUserAddingReaction() {
        Long serverId = 4L;
        AServer server = MockUtils.getServer(serverId);
        AUserInAServer embeddingUser = MockUtils.getUserObject(6L, server);
        AUserInAServer embeddedUser = MockUtils.getUserObject(7L, server);
        executeDeletionTest(serverId, 5L, embeddingUser, embeddedUser, embeddingUser, 1);
    }

    private void executeDeletionTest(Long serverId, Long channelId, AUserInAServer embeddingUser, AUserInAServer embeddedUser, AUserInAServer userAddingReaction, int wantedDeletions) {
        Long messageId = 4L;
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .serverId(serverId)
                .messageId(messageId)
                .channelId(channelId)
                .build();
        AEmote reactedEmote = AEmote.builder().build();
        when(emoteService.getEmoteOrDefaultEmote(MessageEmbedRemovalReactionListener.REMOVAL_EMOTE, serverId)).thenReturn(reactedEmote);
        when(messageReaction.getReactionEmote()).thenReturn(reactionEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, reactedEmote)).thenReturn(true);
        EmbeddedMessage message = EmbeddedMessage
                .builder()
                .embeddingUser(embeddingUser)
                .embeddedUser(embeddedUser)
                .build();
        when(messageEmbedPostManagementService.findEmbeddedPostByMessageId(messageId)).thenReturn(Optional.of(message));
        when(messageService.deleteMessageInChannelInServer(serverId, channelId, messageId)).thenReturn(CompletableFuture.completedFuture(null));
        when(messageEmbedPostManagementService.findEmbeddedPostByMessageId(messageId)).thenReturn(Optional.of(message));
        testUnit.executeReactionAdded(cachedMessage, messageReaction, userAddingReaction);
        verify(messageService, times(wantedDeletions)).deleteMessageInChannelInServer(serverId, channelId, messageId);
        if(wantedDeletions > 0) {
            verify(messageEmbedPostManagementService, times(1)).deleteEmbeddedMessage(message);
        }
    }

    private void executeRemovalAddedTest(boolean wasCorrectEmote, Long messageId) {
        Long serverId = 4L;
        AServer server = MockUtils.getServer(serverId);
        Long channelId = 5L;
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .serverId(serverId)
                .messageId(messageId)
                .channelId(channelId)
                .build();
        AUserInAServer userInAServer = MockUtils.getUserObject(5L, server);
        AEmote reactedEmote = AEmote.builder().build();
        when(emoteService.getEmoteOrDefaultEmote(MessageEmbedRemovalReactionListener.REMOVAL_EMOTE, serverId)).thenReturn(reactedEmote);
        when(messageReaction.getReactionEmote()).thenReturn(reactionEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, reactedEmote)).thenReturn(wasCorrectEmote);
        testUnit.executeReactionAdded(cachedMessage, messageReaction, userInAServer);
        verify(messageService, times(0)).deleteMessageInChannelInServer(serverId, channelId, messageId);
    }

}
