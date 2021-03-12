package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.starboard.config.StarboardFeature;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.service.StarboardService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostReactorManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private MetricService metricService;

    @Mock
    private CachedReactions cachedReaction;

    @Mock
    private CachedMessage cachedMessage;

    @Mock
    private ServerUser serverUserActing;

    @Mock
    private CachedAuthor cachedAuthor;

    @Mock
    private AUserInAServer userInServerActing;

    @Mock
    private AUser userActing;

    @Mock
    private AUserInAServer userInAServer;

    @Mock
    private AUser aUser;

    @Mock
    private AServer server;

    @Mock
    private StarboardPost post;

    @Mock
    private CachedEmote cachedEmote;

    @Mock
    private AEmote starEmote;

    private static final Long MESSAGE_ID = 5L;
    private static final Long SERVER_ID = 6L;
    private static final Long AUTHOR_ID = 4L;
    private static final Long USER_ACTING_ID = 7L;

    @Test
    public void testAuthorAddingStar() {
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(serverUserActing.getUserId()).thenReturn(AUTHOR_ID);
        testUnit.executeReactionAdded(cachedMessage, cachedReaction, serverUserActing);
        verify(emoteService, times(0)).getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID);
    }

    @Test
    public void testAddingWrongEmote() {
        when(serverUserActing.getUserId()).thenReturn(USER_ACTING_ID);
        setupWrongEmote(SERVER_ID, AUTHOR_ID, starEmote);
        when(cachedReaction.getEmote()).thenReturn(cachedEmote);
        when(emoteService.compareCachedEmoteWithAEmote(cachedEmote, starEmote)).thenReturn(false);
        testUnit.executeReactionAdded(cachedMessage, cachedReaction, serverUserActing);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(0)).getReactionFromMessageByEmote(any(CachedMessage.class), eq(starEmote));
    }

    @Test
    public void testAddingEmoteToExistingPostButNowBelowThreshold() {
        Long requiredStars = 5L;
        setupActingAndAuthor();
        executeAddingTest(requiredStars, post);
        verify(starboardService, times(1)).deleteStarboardMessagePost(post);
        verify(starboardPostManagementService, times(1)).removePost(post);
    }

    @Test
    public void testAddingEmoteBelowThreshold() {
        Long requiredStars = 5L;
        setupActingAndAuthor();
        executeAddingTest(requiredStars, null);
        verify(starboardService, times(0)).deleteStarboardMessagePost(any(StarboardPost.class));
        verify(starboardPostManagementService, times(0)).removePost(any(StarboardPost.class));
    }

    @Test
    public void testAddingEmoteReachingThreshold() {
        Long requiredStars = 1L;
        setupActingAndAuthor();
        executeAddingTest(requiredStars, null);
        verify(metricService, times(2)).incrementCounter(any());
        verify(starboardService, times(1)).createStarboardPost(any(CachedMessage.class), anyList(), eq(userInServerActing), eq(userInAServer));
    }

    @Test
    public void testAddingEmoteToExistingPost() {
        Long requiredStars = 1L;
        setupActingAndAuthor();
        executeAddingTest(requiredStars, post);
        verify(metricService, times(1)).incrementCounter(any());
        verify(starboardService, times(1)).updateStarboardPost(eq(post), any(CachedMessage.class), anyList());
        verify(starboardPostReactorManagementService, times(1)).addReactor(post, userInServerActing);
    }

    @Test
    public void testAuthorRemovingReaction() {
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(serverUserActing.getUserId()).thenReturn(AUTHOR_ID);
        testUnit.executeReactionRemoved(cachedMessage, cachedReaction, serverUserActing);
        verify(emoteService, times(0)).getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID);
    }

    @Test
    public void testRemovingWrongEmote() {
        when(serverUserActing.getUserId()).thenReturn(USER_ACTING_ID);
        setupWrongEmote(SERVER_ID, AUTHOR_ID, starEmote);
        testUnit.executeReactionRemoved(cachedMessage, cachedReaction, serverUserActing);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(0)).getReactionFromMessageByEmote(any(CachedMessage.class), eq(starEmote));
    }

    @Test
    public void testRemoveReactionFromExistingPostBelowThreshold() {
        Long requiredStars = 5L;
        List<ServerUser> remainingUsers = Arrays.asList(serverUserActing);
        setupActingAndAuthor();
        executeRemovalTest(requiredStars, remainingUsers);
        verify(starboardService, times(1)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(1)).removePost(eq(post));
    }

    @Test
    public void testRemoveReactionFromExistingPostAboveThreshold() {
        Long requiredStars = 0L;
        List<ServerUser> remainingUsers = Arrays.asList(serverUserActing);
        setupActingAndAuthor();
        when(userInServerManagementService.loadOrCreateUser(serverUserActing)).thenReturn(userInServerActing);
        executeRemovalTest(requiredStars, remainingUsers);
        verify(metricService, times(1)).incrementCounter(any());
        verify(starboardService, times(0)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(0)).removePost(eq(post));
    }

    @Test
    public void testRemoveReactionFromExistingPostTriggeringThreshold() {
        Long requiredStars = 1L;
        ArrayList<ServerUser> usersRemaining = new ArrayList<>();
        setupActingAndAuthor();
        executeRemovalTest(requiredStars, usersRemaining);
        verify(metricService, times(2)).incrementCounter(any());
        verify(starboardService, times(1)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(1)).removePost(eq(post));
    }

    @Test
    public void testReactionsClearedOnStarredMessage() {
        executeClearingTest(Mockito.mock(StarboardPost.class));
    }

    @Test
    public void testReactionsClearedOnNotStarredMessage() {
        executeClearingTest(null);
    }

    private void setupActingAndAuthor() {
        when(userInServerActing.getUserReference()).thenReturn(userActing);
        when(userActing.getId()).thenReturn(USER_ACTING_ID);
        when(userInAServer.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(userInAServer.getUserReference()).thenReturn(aUser);
        when(aUser.getId()).thenReturn(AUTHOR_ID);
    }


    private void executeClearingTest(StarboardPost post) {
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(starboardPostManagementService.findByMessageId(MESSAGE_ID)).thenReturn(Optional.ofNullable(post));
        testUnit.executeReactionCleared(cachedMessage);
        int callCount = post != null ? 1 : 0;
        verify(starboardPostReactorManagementService, times(callCount)).removeReactors(post);
        verify(starboardService, times(callCount)).deleteStarboardMessagePost(eq(post));
        verify(starboardPostManagementService, times(callCount)).removePost(eq(post));
    }


    private void executeRemovalTest(Long requiredStars, List<ServerUser> remainingUsers) {
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(cachedReaction.getEmote()).thenReturn(cachedEmote);
        when(emoteService.compareCachedEmoteWithAEmote(cachedEmote, starEmote)).thenReturn(true);
        when(emoteService.getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID)).thenReturn(starEmote);
        CachedReactions reaction = Mockito.mock(CachedReactions.class);
        when(reaction.getUsers()).thenReturn(remainingUsers);
        when(emoteService.getReactionFromMessageByEmote(cachedMessage, starEmote)).thenReturn(Optional.of(reaction));
        when(starboardPostManagementService.findByMessageId(MESSAGE_ID)).thenReturn(Optional.ofNullable(post));
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, AUTHOR_ID)).thenReturn(userInAServer);
        when(serverUserActing.getUserId()).thenReturn(USER_ACTING_ID);
        when(serverUserActing.getServerId()).thenReturn(SERVER_ID);
        if(!remainingUsers.isEmpty()) {
            when(userInServerManagementService.loadUserOptional(SERVER_ID, USER_ACTING_ID)).thenReturn(Optional.of(userInServerActing));
        }
        AConfig starRequirementConfig = Mockito.mock(AConfig.class);
        when(starRequirementConfig.getLongValue()).thenReturn(requiredStars);
        when(configManagementService.loadConfig(SERVER_ID, StarboardListener.FIRST_LEVEL_THRESHOLD_KEY)).thenReturn(starRequirementConfig);
        testUnit.executeReactionRemoved(cachedMessage, cachedReaction, serverUserActing);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(1)).getReactionFromMessageByEmote(cachedMessage, starEmote);
    }

    private void executeAddingTest(Long requiredStars, StarboardPost postToUse) {
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(cachedReaction.getEmote()).thenReturn(cachedEmote);
        when(emoteService.compareCachedEmoteWithAEmote(cachedEmote, starEmote)).thenReturn(true);
        when(emoteService.getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID)).thenReturn(starEmote);
        CachedReactions reaction = Mockito.mock(CachedReactions.class);
        when(serverUserActing.getUserId()).thenReturn(USER_ACTING_ID);
        when(serverUserActing.getServerId()).thenReturn(SERVER_ID);
        when(reaction.getUsers()).thenReturn(Arrays.asList(serverUserActing));
        when(emoteService.getReactionFromMessageByEmote(cachedMessage, starEmote)).thenReturn(Optional.of(reaction));
        when(starboardPostManagementService.findByMessageId(MESSAGE_ID)).thenReturn(Optional.ofNullable(postToUse));
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, AUTHOR_ID)).thenReturn(userInAServer);
        when(userInServerManagementService.loadOrCreateUser(serverUserActing)).thenReturn(userInServerActing);
        when(userInServerManagementService.loadUserOptional(SERVER_ID, USER_ACTING_ID)).thenReturn(Optional.of(userInServerActing));
        AConfig starRequirementConfig = Mockito.mock(AConfig.class);
        when(starRequirementConfig.getLongValue()).thenReturn(requiredStars);
        when(configManagementService.loadConfig(SERVER_ID, StarboardListener.FIRST_LEVEL_THRESHOLD_KEY)).thenReturn(starRequirementConfig);
        testUnit.executeReactionAdded(cachedMessage, cachedReaction, serverUserActing);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(1)).getReactionFromMessageByEmote(cachedMessage, starEmote);
    }

    private void setupWrongEmote(Long serverId, Long authorId, AEmote starEmote) {
        when(cachedMessage.getServerId()).thenReturn(serverId);
        when(cachedAuthor.getAuthorId()).thenReturn(authorId);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(cachedReaction.getEmote()).thenReturn(cachedEmote);
        when(emoteService.getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, serverId)).thenReturn(starEmote);
    }
}
