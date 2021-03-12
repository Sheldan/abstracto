package dev.sheldan.abstracto.starboard.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.model.database.StarboardPostReaction;
import dev.sheldan.abstracto.starboard.repository.StarboardPostRepository;
import org.junit.Assert;
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
public class StarboardPostManagementServiceBeanTest {

    @InjectMocks
    private StarboardPostManagementServiceBean testUnit;

    @Mock
    private StarboardPostRepository repository;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private AServer server;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private AChannel sourceChannel;

    @Mock
    private AChannel starboardChannel;

    private static final Long SOURCE_CHANNEL_ID = 5L;
    private static final Long SERVER_ID = 7L;

    @Test
    public void testCreateStarboardPost() {
        Long starboardPostId = 5L;
        Long starredMessageId = 8L;
        CachedMessage starredMessage = Mockito.mock(CachedMessage.class);
        when(starredMessage.getServerId()).thenReturn(SERVER_ID);
        when(starredMessage.getChannelId()).thenReturn(SOURCE_CHANNEL_ID);
        when(starredMessage.getMessageId()).thenReturn(starredMessageId);
        AServerAChannelMessage postInStarboard = Mockito.mock(AServerAChannelMessage.class);
        when(postInStarboard.getServer()).thenReturn(server);
        when(postInStarboard.getChannel()).thenReturn(starboardChannel);
        when(postInStarboard.getMessageId()).thenReturn(starboardPostId);
        when(channelManagementService.loadChannel(SOURCE_CHANNEL_ID)).thenReturn(sourceChannel);
        AUser aUser = Mockito.mock(AUser.class);
        when(aUserInAServer.getUserReference()).thenReturn(aUser);
        StarboardPost createdStarboardPost = testUnit.createStarboardPost(starredMessage, aUserInAServer, postInStarboard);
        verify(repository, times(1)).save(createdStarboardPost);
        Assert.assertEquals(starboardChannel, createdStarboardPost.getStarboardChannel());
        Assert.assertEquals(starboardPostId, createdStarboardPost.getStarboardMessageId());
        Assert.assertEquals(starredMessageId, createdStarboardPost.getPostMessageId());
        Assert.assertEquals(aUserInAServer, createdStarboardPost.getAuthor());
        Assert.assertEquals(sourceChannel, createdStarboardPost.getSourceChannel());
        Assert.assertFalse(createdStarboardPost.isIgnored());
    }

    @Test
    public void setStarboardMessageId(){
        StarboardPost post = Mockito.mock(StarboardPost.class);
        Long messageId = 6L;
        testUnit.setStarboardPostMessageId(post, messageId);
        verify(post, times(1)).setStarboardMessageId(messageId);
        verify(repository, times(1)).save(post);
    }

    @Test
    public void testRetrieveTopPosts() {
        Integer count = 2;
        StarboardPost starboardPost1 = Mockito.mock(StarboardPost.class);
        when(starboardPost1.getReactions()).thenReturn(Arrays.asList(Mockito.mock(StarboardPostReaction.class), Mockito.mock(StarboardPostReaction.class)));
        StarboardPost starboardPost2 = Mockito.mock(StarboardPost.class);
        when(starboardPost2.getReactions()).thenReturn(Arrays.asList(Mockito.mock(StarboardPostReaction.class)));
        StarboardPost starboardPost3 = Mockito.mock(StarboardPost.class);
        when(starboardPost3.getReactions()).thenReturn(new ArrayList<>());
        List<StarboardPost> posts = Arrays.asList(starboardPost1, starboardPost2, starboardPost3);
        when(repository.findByServer_Id(SERVER_ID)).thenReturn(posts);
        List<StarboardPost> topPosts = testUnit.retrieveTopPosts(SERVER_ID, count);
        Assert.assertEquals(count.intValue(), topPosts.size());
        StarboardPost topMostPost = topPosts.get(0);
        StarboardPost secondTop = topPosts.get(1);
        Assert.assertEquals(starboardPost1, topPosts.get(0));
        Assert.assertEquals(starboardPost2, secondTop);
        Assert.assertTrue(topMostPost.getReactions().size() > secondTop.getReactions().size());
    }

    @Test
    public void testRetrieveMoreThanAvailable() {
        Integer count = 5;
        StarboardPost starboardPost1 = Mockito.mock(StarboardPost.class);
        when(starboardPost1.getReactions()).thenReturn(Arrays.asList(Mockito.mock(StarboardPostReaction.class), Mockito.mock(StarboardPostReaction.class)));
        StarboardPost starboardPost2 = Mockito.mock(StarboardPost.class);
        when(starboardPost2.getReactions()).thenReturn(Arrays.asList(Mockito.mock(StarboardPostReaction.class)));
        StarboardPost starboardPost3 = Mockito.mock(StarboardPost.class);
        when(starboardPost3.getReactions()).thenReturn(new ArrayList<>());
        List<StarboardPost> posts = Arrays.asList(starboardPost1, starboardPost2, starboardPost3);
        when(repository.findByServer_Id(SERVER_ID)).thenReturn(posts);
        List<StarboardPost> topPosts = testUnit.retrieveTopPosts(SERVER_ID, count);
        StarboardPost topMostPost = topPosts.get(0);
        StarboardPost secondTop = topPosts.get(1);
        StarboardPost thirdTopMostPost = topPosts.get(2);
        Assert.assertEquals(3, topPosts.size());
        Assert.assertEquals(starboardPost1, topPosts.get(0));
        Assert.assertEquals(starboardPost2, secondTop);
        Assert.assertEquals(starboardPost3, thirdTopMostPost);
        Assert.assertTrue(topMostPost.getReactions().size() > secondTop.getReactions().size());
        Assert.assertTrue(secondTop.getReactions().size() > thirdTopMostPost.getReactions().size());
    }

    @Test
    public void testRemovePost() {
        StarboardPost starboardPost1 = Mockito.mock(StarboardPost.class);
        when(starboardPost1.getReactions()).thenReturn(new ArrayList<>(Arrays.asList(Mockito.mock(StarboardPostReaction.class), Mockito.mock(StarboardPostReaction.class))));
        testUnit.removePost(starboardPost1);
        verify(repository, times(1)).delete(any(StarboardPost.class));
    }

    @Test
    public void testSetStarboardPostIgnored() {
        Long messageId = 5L;
        Boolean ignoredValue = true;
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(repository.findByStarboardMessageId(messageId)).thenReturn(post);
        testUnit.setStarboardPostIgnored(messageId, ignoredValue);
        verify(post, times(1)).setIgnored(true);
        verify(repository, times(1)).save(post);
    }

    @Test
    public void testIsStarboardPost() {
        Long starboardPostId = 5L;
        when(repository.existsByStarboardMessageId(starboardPostId)).thenReturn(true);
        boolean starboardPost = testUnit.isStarboardPost(starboardPostId);
        Assert.assertTrue(starboardPost);
    }

    @Test
    public void testFindByMessageId() {
        Long messageId = 5L;
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(repository.findByPostMessageId(messageId)).thenReturn(post);
        Optional<StarboardPost> postOptional = testUnit.findByMessageId(messageId);
        Assert.assertTrue(postOptional.isPresent());
        postOptional.ifPresent(starboardPost -> Assert.assertEquals(starboardPost, post));
    }

    @Test
    public void testFindByMessageIdMissing() {
        Long messageId = 5L;
        when(repository.findByPostMessageId(messageId)).thenReturn(null);
        Optional<StarboardPost> postOptional = testUnit.findByMessageId(messageId);
        Assert.assertFalse(postOptional.isPresent());
    }

    @Test
    public void testFindByStarboardPostId() {
        Long postId = 5L;
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(repository.findByStarboardMessageId(postId)).thenReturn(post);
        Optional<StarboardPost> postOptional = testUnit.findByStarboardPostId(postId);
        Assert.assertTrue(postOptional.isPresent());
        postOptional.ifPresent(starboardPost -> Assert.assertEquals(starboardPost, post));
    }

    @Test
    public void testFindByStarboardPostIdMissing() {
        Long postId = 5L;
        when(repository.findByStarboardMessageId(postId)).thenReturn(null);
        Optional<StarboardPost> postOptional = testUnit.findByStarboardPostId(postId);
        Assert.assertFalse(postOptional.isPresent());
    }

    @Test
    public void testRetrievePostCount() {
        StarboardPost starboardPost1 = Mockito.mock(StarboardPost.class);
        StarboardPost starboardPost2 = Mockito.mock(StarboardPost.class);
        List<StarboardPost> posts = Arrays.asList(starboardPost1, starboardPost2);
        when(repository.findByServer_Id(SERVER_ID)).thenReturn(posts);
        Integer retrievedPostCount = testUnit.getPostCount(SERVER_ID);
        Assert.assertEquals(posts.size(), retrievedPostCount.intValue());
    }
}
