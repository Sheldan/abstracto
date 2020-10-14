package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.service.StarboardService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostReactorManagementService;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardListenerTest {

    @InjectMocks
    private StarboardListener testUnit;

    @Mock
    private BotService botService;

    @Mock
    private ConfigManagementService configManagementService;

    @Mock
    private StarboardService starboardService;

    @Mock
    private StarboardPostManagementService starboardPostManagementService;

    @Mock
    private StarboardPostReactorManagementService starboardPostReactorManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private GuildMessageReactionAddEvent addEvent;

    @Mock
    private GuildMessageReactionRemoveEvent removeEvent;

    @Mock
    private MessageReaction.ReactionEmote reactionEmote;

    @Test
    public void testAuthorAddingStar() {
        Long serverId = 5L;
        Long authorId = 4L;
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .authorId(authorId)
                .serverId(serverId)
                .build();
        AUserInAServer userAdding = MockUtils.getUserObject(authorId, MockUtils.getServer(serverId));
        testUnit.executeReactionAdded(cachedMessage, addEvent, userAdding);
        verify(emoteService, times(0)).getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId);
    }

    @Test
    public void testAddingWrongEmote() {
        Long serverId = 5L;
        Long authorId = 4L;
        Long reactionUserId = 7L;
        AEmote starEmote = AEmote.builder().build();
        AUserInAServer userAdding = MockUtils.getUserObject(reactionUserId, MockUtils.getServer(serverId));
        CachedMessage cachedMessage = setupWrongEmote(serverId, authorId, starEmote);
        testUnit.executeReactionAdded(cachedMessage, addEvent, userAdding);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId);
        verify(emoteService, times(0)).getReactionFromMessageByEmote(any(CachedMessage.class), eq(starEmote));
    }

    @Test
    public void testAddingEmoteToExistingPostButNowBelowThreshold() {
        Long requiredStars = 5L;
        AServer server = MockUtils.getServer();
        AUserInAServer userAdding = MockUtils.getUserObject(7L, server);
        AUserInAServer author = MockUtils.getUserObject(8L, server);
        StarboardPost post = StarboardPost.builder().build();
        executeAddingTest(userAdding, author, requiredStars, post);
        verify(starboardService, times(1)).deleteStarboardMessagePost(post);
        verify(starboardPostManagementService, times(1)).removePost(post);
    }

    @Test
    public void testAddingEmoteBelowThreshold() {
        Long requiredStars = 5L;
        AServer server = MockUtils.getServer();
        AUserInAServer userAdding = MockUtils.getUserObject(7L, server);
        AUserInAServer author = MockUtils.getUserObject(8L, server);
        executeAddingTest(userAdding, author, requiredStars, null);
        verify(starboardService, times(0)).deleteStarboardMessagePost(any(StarboardPost.class));
        verify(starboardPostManagementService, times(0)).removePost(any(StarboardPost.class));
    }

    @Test
    public void testAddingEmoteReachingThreshold() {
        Long requiredStars = 1L;
        AServer server = MockUtils.getServer();
        AUserInAServer userAdding = MockUtils.getUserObject(7L, server);
        AUserInAServer author = MockUtils.getUserObject(8L, server);
        executeAddingTest(userAdding, author, requiredStars, null);
        verify(starboardService, times(1)).createStarboardPost(any(CachedMessage.class), anyList(), eq(userAdding), eq(author));
    }

    @Test
    public void testAddingEmoteToExistingPost() {
        Long requiredStars = 1L;
        AServer server = MockUtils.getServer();
        AUserInAServer userAdding = MockUtils.getUserObject(7L, server);
        AUserInAServer author = MockUtils.getUserObject(8L, server);
        StarboardPost post = StarboardPost.builder().build();
        executeAddingTest(userAdding, author, requiredStars, post);
        verify(starboardService, times(1)).updateStarboardPost(eq(post), any(CachedMessage.class), anyList());
        verify(starboardPostReactorManagementService, times(1)).addReactor(post, userAdding);
    }

    @Test
    public void testAuthorRemovingReaction() {
        Long serverId = 5L;
        Long authorId = 4L;
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .authorId(authorId)
                .serverId(serverId)
                .build();
        AUserInAServer userAdding = MockUtils.getUserObject(authorId, MockUtils.getServer(serverId));
        testUnit.executeReactionRemoved(cachedMessage, removeEvent, userAdding);
        verify(emoteService, times(0)).getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId);
    }

    @Test
    public void testRemovingWrongEmote() {
        Long serverId = 5L;
        Long authorId = 4L;
        Long reactionUserId = 7L;
        AEmote starEmote = AEmote.builder().build();
        AUserInAServer userAdding = MockUtils.getUserObject(reactionUserId, MockUtils.getServer(serverId));
        CachedMessage cachedMessage = setupWrongEmote(serverId, authorId, starEmote);
        testUnit.executeReactionRemoved(cachedMessage, removeEvent, userAdding);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId);
        verify(emoteService, times(0)).getReactionFromMessageByEmote(any(CachedMessage.class), eq(starEmote));
    }

    @Test
    public void testRemoveReactionFromExistingPostBelowThreshold() {
        Long requiredStars = 5L;
        AServer server = MockUtils.getServer();
        AUserInAServer userRemoving = MockUtils.getUserObject(7L, server);
        List<Long> remainingUsers = Arrays.asList(userRemoving.getUserReference().getId());
        AUserInAServer author = MockUtils.getUserObject(8L, server);
        StarboardPost post = StarboardPost.builder().build();
        executeRemovalTest(requiredStars, userRemoving, remainingUsers, userRemoving, author, post);
        verify(starboardService, times(1)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(1)).removePost(eq(post));
    }

    @Test
    public void testRemoveReactionFromExistingPostAboveThreshold() {
        Long requiredStars = 0L;
        AServer server = MockUtils.getServer();
        AUserInAServer remainingUser = MockUtils.getUserObject(9L, server);
        List<Long> remainingUsers = Arrays.asList(remainingUser.getUserReference().getId());
        AUserInAServer userRemoving = MockUtils.getUserObject(7L, server);
        AUserInAServer author = MockUtils.getUserObject(8L, server);
        StarboardPost post = StarboardPost.builder().build();
        executeRemovalTest(requiredStars, remainingUser, remainingUsers, userRemoving, author, post);
        verify(starboardService, times(0)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(0)).removePost(eq(post));
    }

    @Test
    public void testRemoveReactionFromExistingPostTriggeringThreshold() {
        Long requiredStars = 1L;
        AServer server = MockUtils.getServer();
        ArrayList<Long> usersRemaining = new ArrayList<>();
        AUserInAServer userRemoving = MockUtils.getUserObject(7L, server);
        AUserInAServer author = MockUtils.getUserObject(8L, server);
        StarboardPost post = StarboardPost.builder().build();
        executeRemovalTest(requiredStars, userRemoving, usersRemaining, userRemoving, author, post);
        verify(starboardService, times(1)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(1)).removePost(eq(post));
    }

    @Test
    public void testReactionsClearedOnStarredMessage() {
        executeClearingTest(StarboardPost.builder().build());
    }

    @Test
    public void testReactionsClearedOnNotStarredMessage() {
        executeClearingTest(null);
    }

    private void executeClearingTest(StarboardPost post) {
        Long messageId = 5L;
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .messageId(messageId)
                .build();
        when(starboardPostManagementService.findByMessageId(messageId)).thenReturn(Optional.ofNullable(post));
        testUnit.executeReactionCleared(cachedMessage);
        int callCount = post != null ? 1 : 0;
        verify(starboardPostReactorManagementService, times(callCount)).removeReactors(post);
        verify(starboardService, times(callCount)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(callCount)).removePost(eq(post));
    }


    private void executeRemovalTest(Long requiredStars, AUserInAServer remainingUser, List<Long> remainingUsers, AUserInAServer userRemoving, AUserInAServer author, StarboardPost post) {
        Long messageId = 6L;
        Long serverId = userRemoving.getServerReference().getId();
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .authorId(author.getUserReference().getId())
                .serverId(serverId)
                .messageId(messageId)
                .build();
        when(removeEvent.getReactionEmote()).thenReturn(reactionEmote);
        AEmote starEmote = AEmote.builder().build();
        when(emoteService.getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId)).thenReturn(starEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, starEmote)).thenReturn(true);
        CachedReaction reaction = CachedReaction.builder().userInServersIds(remainingUsers).build();
        when(emoteService.getReactionFromMessageByEmote(cachedMessage, starEmote)).thenReturn(Optional.of(reaction));
        when(starboardPostManagementService.findByMessageId(messageId)).thenReturn(Optional.ofNullable(post));
        when(userInServerManagementService.loadUser(serverId, author.getUserReference().getId())).thenReturn(author);
        when(userInServerManagementService.loadUserConditional(remainingUser.getUserReference().getId())).thenReturn(Optional.of(remainingUser));
        when(configManagementService.loadConfig(serverId, StarboardListener.FIRST_LEVEL_THRESHOLD_KEY)).thenReturn(AConfig.builder().longValue(requiredStars).build());
        testUnit.executeReactionRemoved(cachedMessage, removeEvent, userRemoving);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId);
        verify(emoteService, times(1)).getReactionFromMessageByEmote(cachedMessage, starEmote);
    }

    private void executeAddingTest(AUserInAServer userAdding, AUserInAServer author, Long requiredStars, StarboardPost existingPost) {
        Long messageId = 6L;
        Long serverId = userAdding.getServerReference().getId();
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .authorId(author.getUserReference().getId())
                .serverId(serverId)
                .messageId(messageId)
                .build();
        when(addEvent.getReactionEmote()).thenReturn(reactionEmote);
        AEmote starEmote = AEmote.builder().build();
        when(emoteService.getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId)).thenReturn(starEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, starEmote)).thenReturn(true);
        CachedReaction reaction = CachedReaction.builder().userInServersIds(Arrays.asList(userAdding.getUserReference().getId())).build();
        when(emoteService.getReactionFromMessageByEmote(cachedMessage, starEmote)).thenReturn(Optional.of(reaction));
        when(starboardPostManagementService.findByMessageId(messageId)).thenReturn(Optional.ofNullable(existingPost));
        when(userInServerManagementService.loadUser(serverId, author.getUserReference().getId())).thenReturn(author);
        when(userInServerManagementService.loadUserConditional(userAdding.getUserReference().getId())).thenReturn(Optional.of(userAdding));
        when(configManagementService.loadConfig(serverId, StarboardListener.FIRST_LEVEL_THRESHOLD_KEY)).thenReturn(AConfig.builder().longValue(requiredStars).build());
        testUnit.executeReactionAdded(cachedMessage, addEvent, userAdding);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId);
        verify(emoteService, times(1)).getReactionFromMessageByEmote(cachedMessage, starEmote);
    }

    private CachedMessage setupWrongEmote(Long serverId, Long authorId, AEmote starEmote) {
        CachedMessage cachedMessage = CachedMessage
                .builder()
                .authorId(authorId)
                .serverId(serverId)
                .build();
        when(addEvent.getReactionEmote()).thenReturn(reactionEmote);
        when(emoteService.getEmoteOrDefaultEmote(StarboardListener.STAR_EMOTE, serverId)).thenReturn(starEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, starEmote)).thenReturn(false);
        return cachedMessage;
    }
}
