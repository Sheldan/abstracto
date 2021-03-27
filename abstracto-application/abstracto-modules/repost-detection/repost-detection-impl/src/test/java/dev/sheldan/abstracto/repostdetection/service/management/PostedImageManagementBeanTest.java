package dev.sheldan.abstracto.repostdetection.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.exception.PostedImageNotFoundException;
import dev.sheldan.abstracto.repostdetection.model.database.PostedImage;
import dev.sheldan.abstracto.repostdetection.model.database.embed.PostIdentifier;
import dev.sheldan.abstracto.repostdetection.repository.PostedImageRepository;
import dev.sheldan.abstracto.repostdetection.service.RepostServiceBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
public class PostedImageManagementBeanTest {

    @InjectMocks
    private PostedImageManagementBean testUnit;

    @Mock
    private PostedImageRepository postedImageRepository;

    @Mock
    private AUserInAServer aUserInAServer;

    @Mock
    private AServer server;

    @Mock
    private PostedImage postedImage;

    private static final String HASH = "hash";
    private static final Integer INDEX = 1;
    private static final Long MESSAGE_ID = 4L;
    private static final Long SERVER_ID = 3L;
    private static final Integer POSITION = 5;

    @Test
    public void testCreatePost() {
        AServerAChannelAUser serverAChannelAUser = Mockito.mock(AServerAChannelAUser.class);
        AChannel channel = Mockito.mock(AChannel.class);
        when(serverAChannelAUser.getGuild()).thenReturn(server);
        when(serverAChannelAUser.getChannel()).thenReturn(channel);
        when(serverAChannelAUser.getAUserInAServer()).thenReturn(aUserInAServer);
        ArgumentCaptor<PostedImage> postedImageArgumentCaptor = ArgumentCaptor.forClass(PostedImage.class);
        PostedImage savedPost = Mockito.mock(PostedImage.class);
        when(postedImageRepository.save(postedImageArgumentCaptor.capture())).thenReturn(savedPost);
        PostedImage createdPost = testUnit.createPost(serverAChannelAUser, MESSAGE_ID, HASH, INDEX);
        Assert.assertEquals(savedPost, createdPost);
        PostedImage capturedPost = postedImageArgumentCaptor.getValue();
        Assert.assertEquals(HASH, capturedPost.getImageHash());
        Assert.assertEquals(INDEX, capturedPost.getPostId().getPosition());
        Assert.assertEquals(MESSAGE_ID, capturedPost.getPostId().getMessageId());
    }

    @Test
    public void testPostWitHashExists() {
        when(server.getId()).thenReturn(SERVER_ID);
        when(postedImageRepository.existsByImageHashAndServerId(HASH, SERVER_ID)).thenReturn(true);
        Assert.assertTrue(testUnit.postWitHashExists(HASH, server));
    }

    @Test
    public void testGetPostWithHash() {
        when(server.getId()).thenReturn(SERVER_ID);
        when(postedImageRepository.findByImageHashAndServerId(HASH, SERVER_ID)).thenReturn(Optional.of(postedImage));
        java.util.Optional<PostedImage> optionalPostedImage = testUnit.getPostWithHash(HASH, server);
        Assert.assertTrue(optionalPostedImage.isPresent());
        optionalPostedImage.ifPresent(optionalPostedImageObj ->
            Assert.assertEquals(postedImage, optionalPostedImageObj)
        );
    }

    @Test
    public void testMessageHasBeenCovered() {
        when(postedImageRepository.existsByPostId_MessageId(MESSAGE_ID)).thenReturn(true);
        Assert.assertTrue(testUnit.messageHasBeenCovered(MESSAGE_ID));
    }

    @Test
    public void testMessageEmbedsHaveBeenCovered() {
        when(postedImageRepository.existsByPostId_MessageIdAndPostId_PositionGreaterThan(MESSAGE_ID, RepostServiceBean.EMBEDDED_LINK_POSITION_START_INDEX - 1)).thenReturn(true);
        Assert.assertTrue(testUnit.messageEmbedsHaveBeenCovered(MESSAGE_ID));
    }

    @Test
    public void testGetAllFromMessage() {
        when(postedImageRepository.findByPostId_MessageId(MESSAGE_ID)).thenReturn(Arrays.asList(postedImage));
        List<PostedImage> foundPosts = testUnit.getAllFromMessage(MESSAGE_ID);
        Assert.assertEquals(1, foundPosts.size());
        Assert.assertEquals(postedImage, foundPosts.get(0));
    }

    @Test
    public void testGetAllFromMessageEmpty() {
        when(postedImageRepository.findByPostId_MessageId(MESSAGE_ID)).thenReturn(new ArrayList<>());
        List<PostedImage> foundPosts = testUnit.getAllFromMessage(MESSAGE_ID);
        Assert.assertEquals(0, foundPosts.size());
    }

    @Test
    public void testGetPostFromMessageAndPositionOptional() {
        when(postedImageRepository.findById(new PostIdentifier(MESSAGE_ID, POSITION))).thenReturn(Optional.of(postedImage));
        Optional<PostedImage> foundPostedImage = testUnit.getPostFromMessageAndPositionOptional(MESSAGE_ID, POSITION);
        Assert.assertTrue(foundPostedImage.isPresent());
        foundPostedImage.ifPresent(optionalPostedImageObj ->
            Assert.assertEquals(postedImage, optionalPostedImageObj)
        );
    }

    @Test(expected = PostedImageNotFoundException.class)
    public void testGetPostFromMessageAndPositionNotFound() {
        when(postedImageRepository.findById(new PostIdentifier(MESSAGE_ID, POSITION))).thenReturn(Optional.empty());
        testUnit.getPostFromMessageAndPosition(MESSAGE_ID, POSITION);
    }


    @Test
    public void testGetPostFromMessageAndPositionFound() {
        when(postedImageRepository.findById(new PostIdentifier(MESSAGE_ID, POSITION))).thenReturn(Optional.of(postedImage));
        PostedImage foundPostedImage = testUnit.getPostFromMessageAndPosition(MESSAGE_ID, POSITION);
        Assert.assertEquals(postedImage, foundPostedImage);
    }

    @Test
    public void testRemovePostedImagesOf() {
        testUnit.removePostedImagesOf(aUserInAServer);
        verify(postedImageRepository, times(1)).deleteByPoster(aUserInAServer);
    }

    @Test
    public void testRemovePostedImagesIn() {
        testUnit.removedPostedImagesIn(server);
        verify(postedImageRepository, times(1)).deleteByServer(server);
    }
}
