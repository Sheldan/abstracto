package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.database.StarboardPostReaction;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.repository.StarStatsUserResult;
import dev.sheldan.abstracto.utility.repository.StarboardPostReactionRepository;
import dev.sheldan.abstracto.utility.repository.converter.StarStatsUserConverter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testAddReactor() {
        StarboardPost post = StarboardPost.builder().reactions(new ArrayList<>()).build();
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(5L, server);
        testUnit.addReactor(post, userInAServer);
        verify(repository, times(1)).save(reactorCaptor.capture());
        StarboardPostReaction reaction = reactorCaptor.getValue();
        Assert.assertEquals(post, reaction.getStarboardPost());
        Assert.assertEquals(userInAServer, reaction.getReactor());
    }

    @Test
    public void testRemoveReactor() {
        StarboardPost post = StarboardPost.builder().build();
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(5L, server);
        testUnit.removeReactor(post, userInAServer);
        verify(repository, times(1)).deleteByReactorAndStarboardPost(userInAServer, post);
    }

    @Test
    public void testRemoveReactors() {
        StarboardPost post = StarboardPost.builder().reactions(new ArrayList<>()).build();
        testUnit.removeReactors(post);
        verify(repository, times(1)).deleteByStarboardPost(post);
    }

    @Test
    public void testRetrieveStarCount() {
        Long serverId = 5L;
        Integer stars = 5;
        when(repository.getReactionCountByServer(serverId)).thenReturn(stars);
        Integer starCount = testUnit.getStarCount(serverId);
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
        Long serverId = 5L;
        StarStatsUser user1 = Mockito.mock(StarStatsUser.class);
        StarStatsUser user2 = Mockito.mock(StarStatsUser.class);
        setupStarStatsReceiverResult(amountToRetrieve, serverId, user1, user2);
        List<StarStatsUser> starStatsUsers = testUnit.retrieveTopStarReceiver(serverId, amountToRetrieve);
        Assert.assertEquals(expectedAmount, starStatsUsers.size());
        Assert.assertEquals(user1, starStatsUsers.get(0));
        if(amountToRetrieve > 1) {
            Assert.assertEquals(user2, starStatsUsers.get(1));
        }
    }

    private void testTopStarGiver(int expectedAmount, Integer amountToRetrieve) {
        Long serverId = 5L;
        StarStatsUser user1 = Mockito.mock(StarStatsUser.class);
        StarStatsUser user2 = Mockito.mock(StarStatsUser.class);
        setupStarStatsGiverResult(amountToRetrieve, serverId, user1, user2);
        List<StarStatsUser> starStatsUsers = testUnit.retrieveTopStarGiver(serverId, amountToRetrieve);
        Assert.assertEquals(expectedAmount, starStatsUsers.size());
        Assert.assertEquals(user1, starStatsUsers.get(0));
        if(amountToRetrieve > 1) {
            Assert.assertEquals(user2, starStatsUsers.get(1));
        }
    }

    private void setupStarStatsGiverResult(Integer amountToRetrieve, Long serverId, StarStatsUser user1, StarStatsUser user2) {
        StarStatsUserResult result1 = Mockito.mock(StarStatsUserResult.class);
        StarStatsUserResult result2 = Mockito.mock(StarStatsUserResult.class);
        List<StarStatsUserResult> results = Arrays.asList(result1, result2);
        when(repository.findTopStarGiverInServer(serverId, amountToRetrieve)).thenReturn(results);
        List<StarStatsUser> statsUser = new ArrayList<>();
        statsUser.add(user1);
        if (amountToRetrieve > 1) {
            statsUser.add(user2);
        }
        when(converter.convertToStarStatsUser(results, serverId)).thenReturn(statsUser);
    }

    private void setupStarStatsReceiverResult(Integer amountToRetrieve, Long serverId, StarStatsUser user1, StarStatsUser user2) {
        StarStatsUserResult result1 = Mockito.mock(StarStatsUserResult.class);
        StarStatsUserResult result2 = Mockito.mock(StarStatsUserResult.class);
        List<StarStatsUserResult> results = Arrays.asList(result1, result2);
        when(repository.retrieveTopStarReceiverInServer(serverId, amountToRetrieve)).thenReturn(results);
        List<StarStatsUser> statsUser = new ArrayList<>();
        statsUser.add(user1);
        if (amountToRetrieve > 1) {
            statsUser.add(user2);
        }
        when(converter.convertToStarStatsUser(results, serverId)).thenReturn(statsUser);
    }
}
