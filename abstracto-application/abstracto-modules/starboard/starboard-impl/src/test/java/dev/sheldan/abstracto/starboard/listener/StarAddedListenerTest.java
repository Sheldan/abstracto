package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureConfig;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.service.StarboardService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostReactorManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarAddedListenerTest {

    @InjectMocks
    private StarAddedListener testUnit;

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
    private ConfigService configService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private MetricService metricService;

    @Mock
    private MessageReaction reaction;

    @Mock
    private CachedReactions cachedReactions;

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
    private EmojiUnion reactionEmote;

    @Mock
    private AEmote starEmote;

    @Mock
    private User user;

    @Mock
    private Member member;

    @Mock
    private ReactionAddedModel model;

    private static final Long MESSAGE_ID = 5L;
    private static final Long SERVER_ID = 6L;
    private static final Long AUTHOR_ID = 4L;
    private static final Long USER_ACTING_ID = 7L;

    @Before
    public void setup() {
        when(member.getUser()).thenReturn(user);
        when(model.getMemberReacting()).thenReturn(member);
        when(user.isBot()).thenReturn(false);
        when(configService.getLongValueOrConfigDefault(StarboardFeatureConfig.STAR_MAX_DAYS_CONFIG_KEY, SERVER_ID)).thenReturn(1L);
        when(cachedMessage.getTimeCreated()).thenReturn(Instant.now());
    }

    @Test
    public void testAuthorAddingStar() {
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(serverUserActing.getUserId()).thenReturn(AUTHOR_ID);
        when(model.getMessage()).thenReturn(cachedMessage);
        when(model.getUserReacting()).thenReturn(serverUserActing);
        testUnit.execute(model);
        verify(emoteService, times(0)).getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID);
    }

    @Test
    public void testAddingWrongEmote() {
        when(serverUserActing.getUserId()).thenReturn(USER_ACTING_ID);
        setupWrongEmote(SERVER_ID, AUTHOR_ID, starEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, starEmote)).thenReturn(false);
        when(model.getMessage()).thenReturn(cachedMessage);
        when(model.getUserReacting()).thenReturn(serverUserActing);
        when(model.getServerId()).thenReturn(SERVER_ID);
        when(model.getReaction()).thenReturn(reaction);
        when(reaction.getEmoji()).thenReturn(reactionEmote);
        testUnit.execute(model);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(0)).getReactionFromMessageByEmote(any(CachedMessage.class), eq(starEmote));
    }

    @Test
    public void testAddingEmoteToExistingPostButNowBelowThreshold() {
        Long requiredStars = 5L;
        setupActingAndAuthor();
        executeAddingTest(requiredStars, post);
        verify(starboardService, times(1)).deleteStarboardPost(post, serverUserActing);
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
        when(starboardService.createStarboardPost(any(CachedMessage.class), anyList(), eq(userInServerActing), eq(userInAServer))).thenReturn(CompletableFuture.completedFuture(null));
        executeAddingTest(requiredStars, null);
        verify(metricService, times(2)).incrementCounter(any());
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
    public void testAddingEmoteToExistingIgnoredPost() {
        Long requiredStars = 1L;
        setupActingAndAuthor();
        when(post.isIgnored()).thenReturn(true);
        executeAddingTest(requiredStars, post);
        verify(metricService, times(1)).incrementCounter(any());
        verify(starboardService, times(0)).updateStarboardPost(eq(post), any(CachedMessage.class), anyList());
        verify(starboardPostReactorManagementService, times(0)).addReactor(post, userInServerActing);
    }

    private void setupActingAndAuthor() {
        when(userInServerActing.getUserReference()).thenReturn(userActing);
        when(userActing.getId()).thenReturn(USER_ACTING_ID);
        when(userInAServer.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(userInAServer.getUserReference()).thenReturn(aUser);
        when(aUser.getId()).thenReturn(AUTHOR_ID);
    }

    private void executeAddingTest(Long requiredStars, StarboardPost postToUse) {
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(reaction.getEmoji()).thenReturn(reactionEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, starEmote)).thenReturn(true);
        when(emoteService.getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID)).thenReturn(starEmote);
        when(serverUserActing.getUserId()).thenReturn(USER_ACTING_ID);
        when(serverUserActing.getServerId()).thenReturn(SERVER_ID);
        when(starboardPostManagementService.findByMessageId(MESSAGE_ID)).thenReturn(Optional.ofNullable(postToUse));
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, AUTHOR_ID)).thenReturn(userInAServer);
        when(userInServerManagementService.loadOrCreateUser(serverUserActing)).thenReturn(userInServerActing);
        when(userInServerManagementService.loadUserOptional(SERVER_ID, USER_ACTING_ID)).thenReturn(Optional.of(userInServerActing));
        AConfig starRequirementConfig = Mockito.mock(AConfig.class);
        when(starRequirementConfig.getLongValue()).thenReturn(requiredStars);
        when(configManagementService.loadConfig(SERVER_ID, StarboardListener.FIRST_LEVEL_THRESHOLD_KEY)).thenReturn(starRequirementConfig);
        when(model.getMessage()).thenReturn(cachedMessage);
        when(cachedReactions.getUsers()).thenReturn(Arrays.asList(serverUserActing));
        when(emoteService.getReactionFromMessageByEmote(cachedMessage, starEmote)).thenReturn(Optional.of(cachedReactions));
        when(model.getUserReacting()).thenReturn(serverUserActing);
        when(model.getReaction()).thenReturn(reaction);
        when(model.getServerId()).thenReturn(SERVER_ID);
        testUnit.execute(model);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(1)).getReactionFromMessageByEmote(cachedMessage, starEmote);
    }

    private void setupWrongEmote(Long serverId, Long authorId, AEmote starEmote) {
        when(cachedAuthor.getAuthorId()).thenReturn(authorId);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(reaction.getEmoji()).thenReturn(reactionEmote);
        when(emoteService.getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, serverId)).thenReturn(starEmote);
    }
}
