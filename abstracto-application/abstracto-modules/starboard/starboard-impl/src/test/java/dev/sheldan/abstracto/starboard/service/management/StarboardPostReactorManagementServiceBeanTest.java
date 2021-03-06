package dev.sheldan.abstracto.starboard.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.starboard.converter.StarStatsUserConverter;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.model.database.StarboardPostReaction;
import dev.sheldan.abstracto.starboard.model.template.StarStatsUser;
import dev.sheldan.abstracto.starboard.repository.StarboardPostReactionRepository;
import dev.sheldan.abstracto.starboard.repository.result.StarStatsGuildUserResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardPostReactorManagementServiceBeanTest {

    @InjectMocks
    private StarboardPostReactorManagementServiceBean testUnit;

    @Mock
    private StarboardPostReactionRepository repository;

    @Mock
    private StarStatsUserConverter converter;

    @Captor
    private ArgumentCaptor<StarboardPostReaction> reactorCaptor;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private AServer server;

    @Mock
    private AUser aUser;

    private static final Long SERVER_ID = 4L;

    @Test
    public void testAddReactor() {
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(aUserInAServer.getUserReference()).thenReturn(aUser);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        testUnit.addReactor(post, aUserInAServer);
        verify(repository, times(1)).save(reactorCaptor.capture());
        StarboardPostReaction reaction = reactorCaptor.getValue();
        Assert.assertEquals(post, reaction.getStarboardPost());
        Assert.assertEquals(aUserInAServer, reaction.getReactor());
    }

    @Test
    public void testRemoveReactor() {
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(aUserInAServer.getUserReference()).thenReturn(aUser);
        when(aUserInAServer.getServerReference()).thenReturn(server);
        testUnit.removeReactor(post, aUserInAServer);
        verify(repository, times(1)).deleteByReactorAndStarboardPost(aUserInAServer, post);
    }

    @Test
    public void testRemoveReactors() {
        StarboardPost post = Mockito.mock(StarboardPost.class);
        testUnit.removeReactors(post);
        verify(repository, times(1)).deleteByStarboardPost(post);
    }

    @Test
    public void testRetrieveStarCount() {
        Integer stars = 5;
        when(repository.getReactionCountByServer(SERVER_ID)).thenReturn(stars);
        Integer starCount = testUnit.getStarCount(SERVER_ID);
        Assert.assertEquals(stars, starCount);
    }

    @Test
    public void testRetrieveTopStarGiverAllAreAvailable() {
        testTopStarGiver(2, 2);
    }

    @Test
    public void testRetrieveTopStarGiverNotAllAreAvailable() {
        testTopStarGiver(2, 3);
    }

    @Test
    public void testRetrieveTopStarGiverMoreAreAvailable() {
        testTopStarGiver(1, 1);
    }

    @Test
    public void testRetrieveTopStarReceiverAllAreAvailable() {
        testTopStarReceiver(2, 2);
    }

    @Test
    public void testRetrieveTopStarReceiverNotAllAreAvailable() {
        testTopStarReceiver(2, 3);
    }

    @Test
    public void testRetrieveTopStarReceiverMoreAreAvailable() {
        testTopStarReceiver(1, 1);
    }

    private void testTopStarReceiver(int expectedAmount, Integer amountToRetrieve) {
        StarStatsUser user1 = Mockito.mock(StarStatsUser.class);
        StarStatsUser user2 = Mockito.mock(StarStatsUser.class);
        setupStarStatsReceiverResult(amountToRetrieve, SERVER_ID, user1, user2);
        List<CompletableFuture<StarStatsUser>> starStatsUsers = testUnit.retrieveTopStarReceiver(SERVER_ID, amountToRetrieve);
        Assert.assertEquals(expectedAmount, starStatsUsers.size());
        Assert.assertEquals(user1, starStatsUsers.get(0).join());
        if(amountToRetrieve > 1) {
            Assert.assertEquals(user2, starStatsUsers.get(1).join());
        }
    }

    private void testTopStarGiver(int expectedAmount, Integer amountToRetrieve) {
        StarStatsUser user1 = Mockito.mock(StarStatsUser.class);
        StarStatsUser user2 = Mockito.mock(StarStatsUser.class);
        setupStarStatsGiverResult(amountToRetrieve, SERVER_ID, user1, user2);
        List<CompletableFuture<StarStatsUser>> starStatsUsers = testUnit.retrieveTopStarGiver(SERVER_ID, amountToRetrieve);
        Assert.assertEquals(expectedAmount, starStatsUsers.size());
        Assert.assertEquals(user1, starStatsUsers.get(0).join());
        if(amountToRetrieve > 1) {
            Assert.assertEquals(user2, starStatsUsers.get(1).join());
        }
    }

    private void setupStarStatsGiverResult(Integer amountToRetrieve, Long serverId, StarStatsUser user1, StarStatsUser user2) {
        StarStatsGuildUserResult result1 = Mockito.mock(StarStatsGuildUserResult.class);
        StarStatsGuildUserResult result2 = Mockito.mock(StarStatsGuildUserResult.class);
        List<StarStatsGuildUserResult> results = Arrays.asList(result1, result2);
        when(repository.findTopStarGiverInServer(serverId, amountToRetrieve)).thenReturn(results);
        List<CompletableFuture<StarStatsUser>> statsUser = new ArrayList<>();
        statsUser.add(CompletableFuture.completedFuture(user1));
        if (amountToRetrieve > 1) {
            statsUser.add(CompletableFuture.completedFuture(user2));
        }
        when(converter.convertToStarStatsUser(results, serverId)).thenReturn(statsUser);
    }

    private void setupStarStatsReceiverResult(Integer amountToRetrieve, Long serverId, StarStatsUser user1, StarStatsUser user2) {
        StarStatsGuildUserResult result1 = Mockito.mock(StarStatsGuildUserResult.class);
        StarStatsGuildUserResult result2 = Mockito.mock(StarStatsGuildUserResult.class);
        List<StarStatsGuildUserResult> results = Arrays.asList(result1, result2);
        when(repository.retrieveTopStarReceiverInServer(serverId, amountToRetrieve)).thenReturn(results);
        List<CompletableFuture<StarStatsUser>> statsUser = new ArrayList<>();
        statsUser.add(CompletableFuture.completedFuture(user1));
        if (amountToRetrieve > 1) {
            statsUser.add(CompletableFuture.completedFuture(user2));
        }
        when(converter.convertToStarStatsUser(results, serverId)).thenReturn(statsUser);
    }
}
