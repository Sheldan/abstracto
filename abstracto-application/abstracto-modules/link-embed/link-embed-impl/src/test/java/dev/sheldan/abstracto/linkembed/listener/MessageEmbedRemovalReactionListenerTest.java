package dev.sheldan.abstracto.linkembed.listener;

import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import dev.sheldan.abstracto.linkembed.service.management.MessageEmbedPostManagementService;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
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
public class MessageEmbedRemovalReactionListenerTest {

    @InjectMocks
    private MessageEmbedRemovalReactionListener testUnit;

    @Mock
    private MessageEmbedPostManagementService messageEmbedPostManagementService;

    @Mock
    private MessageService messageService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private MetricService metricService;

    @Mock
    private MessageReaction messageReaction;

    @Mock
    private EmojiUnion reactionEmote;

    @Mock
    private AUserInAServer embeddingUser;

    @Mock
    private AUser embeddingAUser;

    @Mock
    private AUserInAServer embeddedUser;

    @Mock
    private AUser embeddedAUser;

    @Mock
    private ServerUser reactingUser;

    @Mock
    private ReactionAddedModel model;

    private static final Long SERVER_ID = 4L;
    private static final Long CHANNEL_ID = 5L;
    private static final Long MESSAGE_ID = 6L;
    private static final Long USER_ID = 3L;

    @Test
    public void testAddingWrongEmote() {
        executeRemovalEmoteAddedTest(false);
        verify(messageEmbedPostManagementService, times(0)).findEmbeddedPostByMessageId(MESSAGE_ID);
    }

    @Test
    public void testAddingCorrectEmoteToWrongMessage() {
        when(messageEmbedPostManagementService.findEmbeddedPostByMessageId(MESSAGE_ID)).thenReturn(Optional.empty());
        executeRemovalEmoteAddedTest(true);
    }

    @Test
    public void testIncorrectUserAddingReaction() {
        when(embeddingUser.getUserReference()).thenReturn(embeddingAUser);
        when(embeddedUser.getUserReference()).thenReturn(embeddedAUser);
        when(embeddingAUser.getId()).thenReturn(USER_ID);
        when(embeddedAUser.getId()).thenReturn(USER_ID + 1);
        when(reactingUser.getUserId()).thenReturn(USER_ID + 2);
        executeDeletionTest(embeddingUser, embeddedUser, 0);
    }

    @Test
    public void testEmbeddedUserAddingReaction() {
        when(embeddedUser.getUserReference()).thenReturn(embeddedAUser);
        when(embeddedAUser.getId()).thenReturn(USER_ID + 1);
        when(embeddingUser.getUserReference()).thenReturn(embeddingAUser);
        when(embeddingAUser.getId()).thenReturn(USER_ID + 3);
        when(reactingUser.getUserId()).thenReturn(USER_ID + 1);
        executeDeletionTest(embeddingUser, embeddedUser, 1);
    }

    @Test
    public void testEmbeddingUserAddingReaction() {
        when(embeddingUser.getUserReference()).thenReturn(embeddingAUser);
        when(embeddedUser.getUserReference()).thenReturn(embeddedAUser);
        when(embeddingAUser.getId()).thenReturn(USER_ID);
        when(embeddedAUser.getId()).thenReturn(USER_ID + 1);
        when(reactingUser.getUserId()).thenReturn(USER_ID);
        executeDeletionTest(embeddingUser, embeddedUser, 1);
    }

    private void executeDeletionTest(AUserInAServer embeddingUser, AUserInAServer embeddedUser, int wantedDeletions) {
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(cachedMessage.getChannelId()).thenReturn(CHANNEL_ID);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        AEmote reactedEmote = Mockito.mock(AEmote.class);
        when(emoteService.getEmoteOrDefaultEmote(MessageEmbedRemovalReactionListener.REMOVAL_EMOTE, SERVER_ID)).thenReturn(reactedEmote);
        when(messageReaction.getEmoji()).thenReturn(reactionEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, reactedEmote)).thenReturn(true);
        EmbeddedMessage message = Mockito.mock(EmbeddedMessage.class);
        when(message.getEmbeddingUser()).thenReturn(embeddingUser);
        when(message.getEmbeddedUser()).thenReturn(embeddedUser);
        when(messageEmbedPostManagementService.findEmbeddedPostByMessageId(MESSAGE_ID)).thenReturn(Optional.of(message));
        when(messageService.deleteMessageInChannelInServer(SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        when(messageEmbedPostManagementService.findEmbeddedPostByMessageId(MESSAGE_ID)).thenReturn(Optional.of(message));
        when(model.getMessage()).thenReturn(cachedMessage);
        when(model.getReaction()).thenReturn(messageReaction);
        when(model.getServerId()).thenReturn(SERVER_ID);
        when(model.getUserReacting()).thenReturn(reactingUser);
        testUnit.execute(model);
        verify(messageService, times(wantedDeletions)).deleteMessageInChannelInServer(SERVER_ID, CHANNEL_ID, MESSAGE_ID);
        if(wantedDeletions > 0) {
            verify(messageEmbedPostManagementService, times(1)).deleteEmbeddedMessage(message);
        }
    }

    private void executeRemovalEmoteAddedTest(boolean wasCorrectEmote) {
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        AEmote reactedEmote = Mockito.mock(AEmote.class);
        when(emoteService.getEmoteOrDefaultEmote(MessageEmbedRemovalReactionListener.REMOVAL_EMOTE, SERVER_ID)).thenReturn(reactedEmote);
        when(messageReaction.getEmoji()).thenReturn(reactionEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, reactedEmote)).thenReturn(wasCorrectEmote);
        when(model.getMessage()).thenReturn(cachedMessage);
        when(model.getReaction()).thenReturn(messageReaction);
        when(model.getServerId()).thenReturn(SERVER_ID);
        testUnit.execute(model);
        verify(messageService, times(0)).deleteMessageInChannelInServer(anyLong(), anyLong(), anyLong());
    }

}
