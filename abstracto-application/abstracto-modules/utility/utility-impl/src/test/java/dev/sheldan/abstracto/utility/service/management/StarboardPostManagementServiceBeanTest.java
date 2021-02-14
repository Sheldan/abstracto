package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.database.StarboardPostReaction;
import dev.sheldan.abstracto.utility.repository.StarboardPostRepository;
import org.junit.Assert;
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
public class StarboardPostManagementServiceBeanTest {

    @InjectMocks
    private StarboardPostManagementServiceBean testUnit;

    @Mock
    private StarboardPostRepository repository;

    @Mock
    private ChannelManagementService channelManagementService;

    @Test
    public void testCreateStarboardPost() {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(7L, server);
        AChannel sourceChannel = MockUtils.getTextChannel(server, 9L);
        AChannel starboardChannel = MockUtils.getTextChannel(server, 10L);
        Long starboardPostId = 5L;
        Long starredMessageId = 8L;
        CachedMessage starredMessage = CachedMessage
                .builder()
                .channelId(sourceChannel.getId())
                .messageId(starredMessageId)
                .serverId(server.getId())
                .build();
        AServerAChannelMessage postInStarboard = AServerAChannelMessage
                .builder()
                .server(server)
                .channel(starboardChannel)
                .messageId(starboardPostId)
                .build();
        when(channelManagementService.loadChannel(starredMessage.getChannelId())).thenReturn(sourceChannel);
        StarboardPost createdStarboardPost = testUnit.createStarboardPost(starredMessage, userInAServer, postInStarboard);
        verify(repository, times(1)).save(createdStarboardPost);
        Assert.assertEquals(postInStarboard.getChannel().getId(), createdStarboardPost.getStarboardChannel().getId());
        Assert.assertEquals(postInStarboard.getServer().getId(), createdStarboardPost.getStarboardChannel().getServer().getId());
        Assert.assertEquals(starboardPostId, createdStarboardPost.getStarboardMessageId());
        Assert.assertEquals(starredMessageId, createdStarboardPost.getPostMessageId());
        Assert.assertEquals(userInAServer.getUserInServerId(), createdStarboardPost.getAuthor().getUserInServerId());
        Assert.assertEquals(sourceChannel.getId(), createdStarboardPost.getSourceChannel().getId());
        Assert.assertFalse(createdStarboardPost.isIgnored());
    }

    @Test
    public void setStarboardMessageId(){
        StarboardPost post = StarboardPost
                .builder()
                .build();
        Long messageId = 6L;
        testUnit.setStarboardPostMessageId(post, messageId);
        Assert.assertEquals(messageId, post.getStarboardMessageId());
        verify(repository, times(1)).save(post);
    }

    @Test
    public void testRetrieveTopPosts() {
        AServer server = MockUtils.getServer();
        Integer count = 2;
        StarboardPostReaction reaction = StarboardPostReaction.builder().build();
        StarboardPost starboardPost1 = StarboardPost.builder().reactions(Arrays.asList(reaction, reaction)).build();
        StarboardPost starboardPost2 = StarboardPost.builder().reactions(Arrays.asList(reaction)).build();
        StarboardPost starboardPost3 = StarboardPost.builder().reactions(new ArrayList<>()).build();
        List<StarboardPost> posts = Arrays.asList(starboardPost1, starboardPost2, starboardPost3);
        when(repository.findByServer_Id(server.getId())).thenReturn(posts);
        List<StarboardPost> topPosts = testUnit.retrieveTopPosts(server.getId(), count);
        Assert.assertEquals(count.intValue(), topPosts.size());
        StarboardPost topMostPost = topPosts.get(0);
        StarboardPost secondTop = topPosts.get(1);
        Assert.assertEquals(starboardPost1, topPosts.get(0));
        Assert.assertEquals(starboardPost2, secondTop);
        Assert.assertTrue(topMostPost.getReactions().size() > secondTop.getReactions().size());
    }

    @Test
    public void testRetrieveMoreThanAvailable() {
        AServer server = MockUtils.getServer();
        Integer count = 5;
        StarboardPostReaction reaction = StarboardPostReaction.builder().build();
        StarboardPost starboardPost1 = StarboardPost.builder().reactions(Arrays.asList(reaction, reaction)).build();
        StarboardPost starboardPost2 = StarboardPost.builder().reactions(Arrays.asList(reaction)).build();
        StarboardPost starboardPost3 = StarboardPost.builder().reactions(new ArrayList<>()).build();
        List<StarboardPost> posts = Arrays.asList(starboardPost1, starboardPost2, starboardPost3);
        when(repository.findByServer_Id(server.getId())).thenReturn(posts);
        List<StarboardPost> topPosts = testUnit.retrieveTopPosts(server.getId(), count);
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
        StarboardPostReaction reaction = StarboardPostReaction.builder().build();
        StarboardPost starboardPost = StarboardPost.builder().reactions(new ArrayList<>(Arrays.asList(reaction, reaction))).build();
        testUnit.removePost(starboardPost);
        Assert.assertEquals(0, starboardPost.getReactions().size());
        verify(repository, times(1)).delete(any(StarboardPost.class));
    }

    @Test
    public void testSetStarboardPostIgnored() {
        Long messageId = 5L;
        Boolean ignoredValue = true;
        StarboardPost post = StarboardPost.builder().build();
        when(repository.findByStarboardMessageId(messageId)).thenReturn(post);
        testUnit.setStarboardPostIgnored(messageId, ignoredValue);
        Assert.assertTrue(post.isIgnored());
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
        StarboardPost post = StarboardPost.builder().build();
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
        StarboardPost post = StarboardPost.builder().build();
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
        AServer server = MockUtils.getServer();
        StarboardPost starboardPost1 = StarboardPost.builder().build();
        StarboardPost starboardPost2 = StarboardPost.builder().build();
        List<StarboardPost> posts = Arrays.asList(starboardPost1, starboardPost2);
        when(repository.findByServer_Id(server.getId())).thenReturn(posts);
        Integer retrievedPostCount = testUnit.getPostCount(server.getId());
        Assert.assertEquals(posts.size(), retrievedPostCount.intValue());
    }
}
