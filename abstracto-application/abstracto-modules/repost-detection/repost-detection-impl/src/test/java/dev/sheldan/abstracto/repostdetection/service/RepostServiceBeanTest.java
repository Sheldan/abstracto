package dev.sheldan.abstracto.repostdetection.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.cache.*;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FileService;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionFeatureDefinition;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionFeatureMode;
import dev.sheldan.abstracto.repostdetection.converter.RepostLeaderBoardConverter;
import dev.sheldan.abstracto.repostdetection.model.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.repostdetection.model.database.PostedImage;
import dev.sheldan.abstracto.repostdetection.model.database.Repost;
import dev.sheldan.abstracto.repostdetection.model.database.embed.PostIdentifier;
import dev.sheldan.abstracto.repostdetection.model.database.result.RepostLeaderboardResult;
import dev.sheldan.abstracto.repostdetection.service.management.PostedImageManagement;
import dev.sheldan.abstracto.repostdetection.service.management.RepostManagementService;
import net.dv8tion.jda.api.entities.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostServiceBeanTest {

    @InjectMocks
    private RepostServiceBean testUnit;

    @Mock
    private HttpService httpService;

    @Mock
    private HashService hashService;

    @Mock
    private FileService fileService;

    @Mock
    private PostedImageManagement postedImageManagement;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private ReactionService reactionService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private RepostManagementService repostManagementService;

    @Mock
    private RepostLeaderBoardConverter leaderBoardConverter;

    @Mock
    private RepostServiceBean self;

    @Mock
    private AUserInAServer userInAServer;

    @Mock
    private AUser user;

    @Mock
    private Guild guild;

    @Mock
    private AServer server;

    @Mock
    private AChannel channel;

    @Mock
    private PostedImage postedImage;

    @Mock
    private Repost repost;

    @Mock
    private CachedMessage cachedMessage;

    @Mock
    private Message message;

    @Mock
    private MessageChannel messageChannel;

    @Mock
    private CachedAuthor author;

    private static final Long SERVER_ID = 4L;
    private static final Long CHANNEL_ID = 8L;
    private static final Long MESSAGE_ID = 5L;
    private static final Integer POSITION = 6;
    private static final Long USER_ID = 7L;
    private static final String URL = "url";
    private static final String HASH = "hash";

    @Test
    public void testPurgeRepostsForUser() {
        testUnit.purgeReposts(userInAServer);
        verify(repostManagementService, times(1)).deleteRepostsFromUser(userInAServer);
    }

    @Test
    public void testPurgeRepostsForServer() {
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        testUnit.purgeReposts(guild);
        verify(repostManagementService, times(1)).deleteRepostsFromServer(server);
    }

    @Test
    public void testRetrieveRepostLeaderboard() {
        Integer page = 4;
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        RepostLeaderboardResult result = Mockito.mock(RepostLeaderboardResult.class);
        List<RepostLeaderboardResult> resultList = Arrays.asList(result);
        when(repostManagementService.findTopRepostingUsersOfServer(server, page, RepostServiceBean.LEADER_BOARD_PAGE_SIZE)).thenReturn(resultList);
        RepostLeaderboardEntryModel firstModel = Mockito.mock(RepostLeaderboardEntryModel.class);
        RepostLeaderboardEntryModel secondModel = Mockito.mock(RepostLeaderboardEntryModel.class);
        List<RepostLeaderboardEntryModel> entries = Arrays.asList(firstModel, secondModel);
        when(leaderBoardConverter.fromLeaderBoardResults(resultList)).thenReturn(CompletableFuture.completedFuture(entries));
        CompletableFuture<List<RepostLeaderboardEntryModel>> future = testUnit.retrieveRepostLeaderboard(guild, page);
        Assert.assertTrue(future.isDone());
        List<RepostLeaderboardEntryModel> futureList = future.join();
        Assert.assertEquals(2, futureList.size());
        Assert.assertEquals(firstModel, futureList.get(0));
        Assert.assertEquals(secondModel, futureList.get(1));
    }

    @Test
    public void testPersistRepostWithExisting() {
        Integer count = 4;
        when(postedImageManagement.getPostFromMessageAndPosition(MESSAGE_ID, POSITION)).thenReturn(postedImage);
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, USER_ID)).thenReturn(userInAServer);
        when(repost.getCount()).thenReturn(count);
        when(repostManagementService.findRepostOptional(postedImage, userInAServer)).thenReturn(Optional.of(repost));
        testUnit.persistRepost(MESSAGE_ID, POSITION, SERVER_ID, USER_ID);
        verify(repost, times(1)).setCount(count + 1);
    }

    @Test
    public void testPersistRepostWithoutExisting() {
        when(postedImageManagement.getPostFromMessageAndPosition(MESSAGE_ID, POSITION)).thenReturn(postedImage);
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, USER_ID)).thenReturn(userInAServer);
        when(repostManagementService.findRepostOptional(postedImage, userInAServer)).thenReturn(Optional.empty());
        testUnit.persistRepost(MESSAGE_ID, POSITION, SERVER_ID, USER_ID);
        verify(repostManagementService, times(1)).createRepost(postedImage, userInAServer);
    }

    @Test
    public void testCalculateHashForPostWithImage() throws IOException {
        File file = Mockito.mock(File.class);
        when(featureModeService.featureModeActive(RepostDetectionFeatureDefinition.REPOST_DETECTION, SERVER_ID, RepostDetectionFeatureMode.DOWNLOAD)).thenReturn(true);
        when(httpService.downloadFileToTempFile(URL)).thenReturn(file);
        when(hashService.sha256HashFileContent(file)).thenReturn(HASH);
        String calculatedHash = testUnit.calculateHashForPost(URL, SERVER_ID);
        Assert.assertEquals(HASH, calculatedHash);
        verify(fileService, times(1)).safeDelete(file);
    }

    @Test
    public void testCalculateHashForPostWithImageException() throws IOException {
        File file = Mockito.mock(File.class);
        when(featureModeService.featureModeActive(RepostDetectionFeatureDefinition.REPOST_DETECTION, SERVER_ID, RepostDetectionFeatureMode.DOWNLOAD)).thenReturn(true);
        when(httpService.downloadFileToTempFile(URL)).thenReturn(file);
        when(hashService.sha256HashFileContent(file)).thenThrow(new IOException());
        testUnit.calculateHashForPost(URL, SERVER_ID);
        verify(fileService, times(1)).safeDelete(file);
    }

    @Test
    public void testCalculateHashForPostWithUrl() {
        when(featureModeService.featureModeActive(RepostDetectionFeatureDefinition.REPOST_DETECTION, SERVER_ID, RepostDetectionFeatureMode.DOWNLOAD)).thenReturn(false);
        when(hashService.sha256HashString(URL)).thenReturn(HASH);
        String calculatedHash = testUnit.calculateHashForPost(URL, SERVER_ID);
        Assert.assertEquals(HASH, calculatedHash);
    }

    @Test
    public void testProcessMessageAttachmentRepostCheckNoAttachment() {
        when(cachedMessage.getAttachments()).thenReturn(new ArrayList<>());
        testUnit.processMessageAttachmentRepostCheck(cachedMessage);
        verify(cachedMessage, times(0)).getChannelId();
    }

    @Test
    public void testProcessMessageAttachmentRepostCheckOneAttachmentNotExistingPost() {
        generalSetupForRepostTest();
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(author.getAuthorId()).thenReturn(USER_ID);
        when(cachedMessage.getAuthor()).thenReturn(author);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        CachedAttachment attachment = Mockito.mock(CachedAttachment.class);
        when(cachedMessage.getAttachments()).thenReturn(Arrays.asList(attachment));
        when(attachment.getProxyUrl()).thenReturn(URL);
        setupSingleRepost();
        testUnit.processMessageAttachmentRepostCheck(cachedMessage);
        verify(postedImageManagement, times(1)).createPost(any(AServerAChannelAUser.class), eq(MESSAGE_ID), eq(HASH), eq(0));
        verify(reactionService, times(0)).addReactionToMessageAsync(RepostServiceBean.REPOST_MARKER_EMOTE_KEY, SERVER_ID, CHANNEL_ID, MESSAGE_ID);
    }

    @Test
    public void testProcessMessageAttachmentRepostCheckOneAttachmentIsRepost() {
        generalSetupForRepostTest();
        setupForRepostCreation();
        CachedAttachment attachment = Mockito.mock(CachedAttachment.class);
        when(cachedMessage.getAttachments()).thenReturn(Arrays.asList(attachment));
        when(attachment.getProxyUrl()).thenReturn(URL);
        setupSingleHash(postedImage);
        Long originalPostMessageId = MESSAGE_ID + 1;
        when(postedImage.getPostId()).thenReturn(new PostIdentifier(originalPostMessageId, POSITION));
        when(reactionService.addReactionToMessageAsync(RepostServiceBean.REPOST_MARKER_EMOTE_KEY, SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.processMessageAttachmentRepostCheck(cachedMessage);
        verify(reactionService, times(0)).addDefaultReactionToMessageAsync(anyString(), eq(SERVER_ID), eq(CHANNEL_ID), eq(MESSAGE_ID));
        verify(self, times(1)).persistRepost(originalPostMessageId, POSITION, SERVER_ID, USER_ID);
    }

    @Test
    public void testProcessMessageAttachmentRepostCheckTwoAttachmentsOneIsRepost() {
        generalSetupForRepostTest();
        setupForRepostCreation();
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getAuthor().getAuthorId()).thenReturn(USER_ID);
        CachedAttachment attachment = Mockito.mock(CachedAttachment.class);
        CachedAttachment attachment2 = Mockito.mock(CachedAttachment.class);
        when(cachedMessage.getAttachments()).thenReturn(Arrays.asList(attachment, attachment2));
        when(attachment.getProxyUrl()).thenReturn(URL);
        setupSingleRepost();
        String secondAttachmentUrl = URL + "2";
        when(attachment2.getProxyUrl()).thenReturn(secondAttachmentUrl);
        String secondAttachmentHash = HASH + "2";
        when(hashService.sha256HashString(secondAttachmentUrl)).thenReturn(secondAttachmentHash);
        when(postedImageManagement.getPostWithHash(secondAttachmentHash, server)).thenReturn(Optional.of(postedImage));
        Long originalPostMessageId = MESSAGE_ID + 1;
        when(postedImage.getPostId()).thenReturn(new PostIdentifier(originalPostMessageId, POSITION));
        when(reactionService.addReactionToMessageAsync(RepostServiceBean.REPOST_MARKER_EMOTE_KEY, SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        when(reactionService.addDefaultReactionToMessageAsync(anyString(), eq(SERVER_ID), eq(CHANNEL_ID), eq(MESSAGE_ID))).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.processMessageAttachmentRepostCheck(cachedMessage);
        verify(postedImageManagement, times(1)).createPost(any(AServerAChannelAUser.class), eq(MESSAGE_ID), eq(HASH), eq(0));
        verify(self, times(1)).persistRepost(originalPostMessageId, POSITION, SERVER_ID, USER_ID);
    }

    private void setupForRepostCreation() {
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getAuthor()).thenReturn(author);
        when(author.getAuthorId()).thenReturn(USER_ID);
        when(cachedMessage.getChannelId()).thenReturn(CHANNEL_ID);
    }

    private void generalSetupForRepostTest() {
        when(userInServerManagementService.loadOrCreateUser(SERVER_ID, USER_ID)).thenReturn(userInAServer);
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
    }

    private void setupSingleRepost() {
        setupSingleHash(null);
        when(userInAServer.getUserReference()).thenReturn(user);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(channel);
    }

    private void setupSingleHash(PostedImage postedImage) {
        when(featureModeService.featureModeActive(RepostDetectionFeatureDefinition.REPOST_DETECTION, SERVER_ID, RepostDetectionFeatureMode.DOWNLOAD)).thenReturn(false);
        when(hashService.sha256HashString(URL)).thenReturn(HASH);
        when(postedImageManagement.getPostWithHash(HASH, server)).thenReturn(Optional.ofNullable(postedImage));
    }

    @Test
    public void testProcessMessageEmbedsRepostCheckWithNotRepostedThumbnailNoAttachments() {
        generalSetupForRepostTest();
        CachedEmbed firstEmbed = Mockito.mock(CachedEmbed.class);
        CachedThumbnail thumbnail = Mockito.mock(CachedThumbnail.class);
        when(thumbnail.getProxyUrl()).thenReturn(URL);
        when(firstEmbed.getCachedThumbnail()).thenReturn(thumbnail);
        List<CachedEmbed> messageEmbeds = Arrays.asList(firstEmbed);
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getAuthor()).thenReturn(author);
        when(author.getAuthorId()).thenReturn(USER_ID);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(cachedMessage.getAttachments()).thenReturn(new ArrayList<>());
        setupSingleRepost();
        testUnit.processMessageEmbedsRepostCheck(messageEmbeds, cachedMessage);
        verify(postedImageManagement, times(1)).createPost(any(AServerAChannelAUser.class), eq(MESSAGE_ID), eq(HASH), eq(RepostServiceBean.EMBEDDED_LINK_POSITION_START_INDEX));
        verify(reactionService, times(0)).addReactionToMessageAsync(RepostServiceBean.REPOST_MARKER_EMOTE_KEY, SERVER_ID, CHANNEL_ID, MESSAGE_ID);
    }

    @Test
    public void testProcessMessageEmbedsRepostCheckWithNotRepostedEmbedImageNoAttachments() {
        generalSetupForRepostTest();
        CachedEmbed firstEmbed = Mockito.mock(CachedEmbed.class);
        CachedImageInfo image = Mockito.mock(CachedImageInfo.class);
        when(image.getProxyUrl()).thenReturn(URL);
        when(firstEmbed.getCachedImageInfo()).thenReturn(image);
        List<CachedEmbed> messageEmbeds = Arrays.asList(firstEmbed);
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(author.getAuthorId()).thenReturn(USER_ID);
        when(cachedMessage.getAuthor()).thenReturn(author);
        when(cachedMessage.getMessageId()).thenReturn(MESSAGE_ID);
        when(cachedMessage.getAttachments()).thenReturn(new ArrayList<>());
        setupSingleRepost();
        testUnit.processMessageEmbedsRepostCheck(messageEmbeds, cachedMessage);
        verify(postedImageManagement, times(1)).createPost(any(AServerAChannelAUser.class), eq(MESSAGE_ID), eq(HASH), eq(RepostServiceBean.EMBEDDED_LINK_POSITION_START_INDEX));
        verify(reactionService, times(0)).addReactionToMessageAsync(RepostServiceBean.REPOST_MARKER_EMOTE_KEY, SERVER_ID, CHANNEL_ID, MESSAGE_ID);
    }

    @Test
    public void testProcessMessageEmbedsRepostCheckWithRepostedThumbnailNoAttachments() {
        generalSetupForRepostTest();
        setupForRepostCreation();
        CachedEmbed firstEmbed = Mockito.mock(CachedEmbed.class);
        CachedThumbnail thumbnail = Mockito.mock(CachedThumbnail.class);
        when(thumbnail.getProxyUrl()).thenReturn(URL);
        when(firstEmbed.getCachedThumbnail()).thenReturn(thumbnail);
        List<CachedEmbed> messageEmbeds = Arrays.asList(firstEmbed);
        when(cachedMessage.getAttachments()).thenReturn(new ArrayList<>());
        setupSingleHash(postedImage);
        Long originalPostMessageId = MESSAGE_ID + 1;
        when(postedImage.getPostId()).thenReturn(new PostIdentifier(originalPostMessageId, POSITION));
        when(reactionService.addReactionToMessageAsync(RepostServiceBean.REPOST_MARKER_EMOTE_KEY, SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.processMessageEmbedsRepostCheck(messageEmbeds, cachedMessage);
        verify(self, times(1)).persistRepost(originalPostMessageId, POSITION, SERVER_ID, USER_ID);
    }

    @Test
    public void testIsRepostEmptyEmbedMessage() {
        CachedEmbed messageEmbed = Mockito.mock(CachedEmbed.class);
        Optional<PostedImage> emptyOptional = testUnit.getRepostFor(cachedMessage, messageEmbed, POSITION);
        Assert.assertFalse(emptyOptional.isPresent());
    }

    @Test
    public void testGetRepostForWithRepostSameMessage() {
        executeGetRepostForWithMessageId(MESSAGE_ID, false);
    }

    @Test
    public void testGetRepostForWithRepost() {
        executeGetRepostForWithMessageId(MESSAGE_ID + 1, true);
    }

    private void executeGetRepostForWithMessageId(Long originalPostMessageId, boolean shouldBePresent) {
        CachedEmbed messageEmbed = setupSimpleRepostCheck(originalPostMessageId);
        Optional<PostedImage> optional = testUnit.getRepostFor(cachedMessage, messageEmbed, POSITION);
        Assert.assertEquals(shouldBePresent, optional.isPresent());
        if(shouldBePresent && optional.isPresent()) {
            Assert.assertEquals(postedImage, optional.get());
        }
    }

    private CachedEmbed setupSimpleRepostCheck(Long originalPostMessageId) {
        generalSetupForRepostTest();
        setupForRepostCreation();
        setupSingleHash(postedImage);
        when(postedImage.getPostId()).thenReturn(new PostIdentifier(originalPostMessageId, POSITION));
        CachedEmbed messageEmbed = Mockito.mock(CachedEmbed.class);
        CachedThumbnail thumbnail = Mockito.mock(CachedThumbnail.class);
        when(thumbnail.getProxyUrl()).thenReturn(URL);
        when(messageEmbed.getCachedThumbnail()).thenReturn(thumbnail);
        return messageEmbed;
    }

    @Test
    public void testIsRepostWithRepost() {
        CachedEmbed messageEmbed = setupSimpleRepostCheck(MESSAGE_ID + 1);
        Assert.assertTrue(testUnit.isRepost(cachedMessage, messageEmbed, POSITION));
    }

    @Test
    public void testIsRepostWithSameMessage() {
        CachedEmbed messageEmbed = setupSimpleRepostCheck(MESSAGE_ID);
        Assert.assertFalse(testUnit.isRepost(cachedMessage, messageEmbed, POSITION));
    }

    @Test
    public void testIsRepostWithRepostInAttachment() {
        generalSetupForRepostTest();
        setupSingleRepost();
        setupSingleHash(postedImage);
        when(postedImage.getPostId()).thenReturn(new PostIdentifier(MESSAGE_ID + 1, POSITION));
        CachedAttachment attachment = Mockito.mock(CachedAttachment.class);
        when(attachment.getProxyUrl()).thenReturn(URL);
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(author.getAuthorId()).thenReturn(USER_ID);
        when(cachedMessage.getAuthor()).thenReturn(author);
        Assert.assertTrue(testUnit.isRepost(cachedMessage, attachment, POSITION));
    }

    @Test
    public void testGetRepostForEmpty() {
        Optional<PostedImage> repost = testUnit.getRepostFor(message, Mockito.mock(MessageEmbed.class), 1);
        Assert.assertFalse(repost.isPresent());
    }

    @Test
    public void testGetRepostForMessageThumbnail() {
        MessageEmbed messageEmbed = Mockito.mock(MessageEmbed.class);
        MessageEmbed.Thumbnail thumbnail = Mockito.mock(MessageEmbed.Thumbnail.class);
        when(thumbnail.getProxyUrl()).thenReturn(URL);
        when(messageEmbed.getThumbnail()).thenReturn(thumbnail);
        generalSetupForRepostTest();
        setupForRepostCreation();
        setupSingleHash(postedImage);
        when(postedImage.getPostId()).thenReturn(new PostIdentifier(2L, POSITION));
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        User author = Mockito.mock(User.class);
        when(author.getIdLong()).thenReturn(USER_ID);
        when(message.getAuthor()).thenReturn(author);
        when(message.getChannel()).thenReturn(messageChannel);
        when(messageChannel.getIdLong()).thenReturn(CHANNEL_ID);
        Optional<PostedImage> repost = testUnit.getRepostFor(message, messageEmbed, 1);
        Assert.assertTrue(repost.isPresent());
        repost.ifPresent(postedImage1 -> Assert.assertEquals(postedImage, postedImage1));
    }

    @Test
    public void testProcessMessageEmbedsRepostCheck() {
        MessageEmbed messageEmbed = Mockito.mock(MessageEmbed.class);
        MessageEmbed.ImageInfo imageInfo = Mockito.mock(MessageEmbed.ImageInfo.class);
        when(imageInfo.getProxyUrl()).thenReturn(URL);
        when(messageEmbed.getImage()).thenReturn(imageInfo);
        generalSetupForRepostTest();
        setupForRepostCreation();
        setupSingleHash(postedImage);
        when(postedImage.getPostId()).thenReturn(new PostIdentifier(2L, POSITION));
        when(message.getGuild()).thenReturn(guild);
        when(message.getIdLong()).thenReturn(MESSAGE_ID);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        User author = Mockito.mock(User.class);
        when(author.getIdLong()).thenReturn(USER_ID);
        when(message.getAuthor()).thenReturn(author);
        when(message.getChannel()).thenReturn(messageChannel);
        when(messageChannel.getIdLong()).thenReturn(CHANNEL_ID);
        when(reactionService.addReactionToMessageAsync(RepostServiceBean.REPOST_MARKER_EMOTE_KEY, SERVER_ID, CHANNEL_ID, MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.processMessageEmbedsRepostCheck(Collections.singletonList(messageEmbed), message);
        verify(self, times(1)).persistRepost(2L, POSITION, SERVER_ID, USER_ID);
    }


}
