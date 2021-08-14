package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.listener.ReactionRemovedModel;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureConfig;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.service.StarboardService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostReactorManagementService;
import net.dv8tion.jda.api.entities.MessageReaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarRemovedListenerTest {

    @InjectMocks
    private StarRemovedListener testUnit;

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
    private CachedReactions cachedReactions;

    @Mock
    private AUserInAServer userInAServer;

    @Mock
    private AUser aUser;

    @Mock
    private AServer server;

    @Mock
    private StarboardPost post;

    @Mock
    private MessageReaction.ReactionEmote reactionEmote;

    @Mock
    private AEmote starEmote;

    @Mock
    private ReactionRemovedModel model;

    private static final Long MESSAGE_ID = 5L;
    private static final Long SERVER_ID = 6L;
    private static final Long AUTHOR_ID = 4L;
    private static final Long USER_ACTING_ID = 7L;

    @Before
    public void setup() {
        when(configService.getLongValueOrConfigDefault(StarboardFeatureConfig.STAR_MAX_DAYS_CONFIG_KEY, SERVER_ID)).thenReturn(1L);
        when(cachedMessage.getTimeCreated()).thenReturn(Instant.now());
    }

    @Test
    public void testAuthorRemovingReaction() {
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(serverUserActing.getUserId()).thenReturn(AUTHOR_ID);
        when(model.getMessage()).thenReturn(cachedMessage);
        when(model.getUserRemoving()).thenReturn(serverUserActing);
        testUnit.execute(model);
        verify(emoteService, times(0)).getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID);
    }

    @Test
    public void testRemovingWrongEmote() {
        when(serverUserActing.getUserId()).thenReturn(USER_ACTING_ID);
        setupWrongEmote(SERVER_ID, AUTHOR_ID, starEmote);
        when(model.getMessage()).thenReturn(cachedMessage);
        when(model.getUserRemoving()).thenReturn(serverUserActing);
        when(model.getReaction()).thenReturn(reaction);
        when(reaction.getReactionEmote()).thenReturn(reactionEmote);
        when(model.getServerId()).thenReturn(SERVER_ID);
        testUnit.execute(model);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(0)).getReactionFromMessageByEmote(any(CachedMessage.class), eq(starEmote));
    }

    @Test
    public void testRemoveReactionFromExistingPostBelowThreshold() {
        Long requiredStars = 5L;
        List<ServerUser> remainingUsers = Arrays.asList(serverUserActing);
        setupActingAndAuthor();
        executeRemovalTest(requiredStars, remainingUsers);
        verify(starboardService, times(1)).deleteStarboardPost(post, serverUserActing);
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
        verify(starboardService, times(1)).deleteStarboardPost(post, serverUserActing);
    }

    private void setupActingAndAuthor() {
        when(userInServerActing.getUserReference()).thenReturn(userActing);
        when(userActing.getId()).thenReturn(USER_ACTING_ID);
        when(userInAServer.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(userInAServer.getUserReference()).thenReturn(aUser);
        when(aUser.getId()).thenReturn(AUTHOR_ID);
    }



    private void executeRemovalTest(Long requiredStars, List<ServerUser> remainingUsers) {
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(cachedAuthor.getAuthorId()).thenReturn(AUTHOR_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(reaction.getReactionEmote()).thenReturn(reactionEmote);
        when(emoteService.isReactionEmoteAEmote(reactionEmote, starEmote)).thenReturn(true);
        when(emoteService.getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID)).thenReturn(starEmote);
        when(cachedReactions.getUsers()).thenReturn(remainingUsers);
        when(emoteService.getReactionFromMessageByEmote(cachedMessage, starEmote)).thenReturn(Optional.of(cachedReactions));
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
        when(model.getMessage()).thenReturn(cachedMessage);
        when(model.getUserRemoving()).thenReturn(serverUserActing);
        when(model.getReaction()).thenReturn(reaction);
        when(model.getServerId()).thenReturn(SERVER_ID);
        testUnit.execute(model);
        verify(emoteService, times(1)).getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, SERVER_ID);
        verify(emoteService, times(1)).getReactionFromMessageByEmote(cachedMessage, starEmote);
    }


    private void setupWrongEmote(Long serverId, Long authorId, AEmote starEmote) {
        when(cachedAuthor.getAuthorId()).thenReturn(authorId);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(reaction.getReactionEmote()).thenReturn(reactionEmote);
        when(emoteService.getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, serverId)).thenReturn(starEmote);
    }
}
