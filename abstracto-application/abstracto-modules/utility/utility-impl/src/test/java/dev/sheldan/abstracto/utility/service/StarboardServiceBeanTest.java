package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.posttargets.StarboardPostTarget;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.database.StarboardPostReaction;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsModel;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarboardPostModel;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostReactorManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.entities.GuildImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.utility.service.StarboardServiceBean.STARBOARD_POST_TEMPLATE;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardServiceBeanTest {

    @InjectMocks
    private StarboardServiceBean testUnit;

    @Mock
    private BotService botService;

    @Mock
    private PostTargetService postTargetService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ConfigService configService;

    @Mock
    private StarboardPostManagementService starboardPostManagementService;

    @Mock
    private StarboardPostReactorManagementService starboardPostReactorManagementService;

    @Mock
    private DefaultConfigManagementService defaultConfigManagementService;

    @Mock
    private PostTargetManagement postTargetManagement;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private StarboardServiceBean self;

    @Mock
    private GuildImpl guild;

    @Mock
    private Message sendPost;

    @Mock
    private TextChannel mockedTextChannel;

    @Mock
    private Member starredMember;

    @Mock
    private AChannel starboardChannel;

    @Mock
    private MessageToSend messageToSend;
    
    @Mock
    private AServer server;

    private static final Long STARRED_USER_ID = 5L;
    private static final Long STARRED_SERVER_USER_ID = 2L;
    private static final Long SERVER_ID = 6L;
    private static final Long STARBOARD_CHANNEL_ID = 8L;
    private static final Long FIRST_USER_IN_SERVER_ID = 3L;
    private static final Long SECOND_USER_IN_SERVER_ID = 9L;
    private static final Long CHANNEL_ID = 10L;

    @Captor
    private ArgumentCaptor<AUserInAServer> userInAServerArgumentCaptor;

    @Captor
    private ArgumentCaptor<StarboardPostModel> starboardPostModelArgumentCaptor;

    @Test
    public void testCreateStarboardPost() {
        List<AUserInAServer> userExceptAuthor = new ArrayList<>();
        AUserInAServer firstUserExceptAuthor = Mockito.mock(AUserInAServer.class);
        userExceptAuthor.add(firstUserExceptAuthor);
        AUserInAServer secondUserExceptAuthor = Mockito.mock(AUserInAServer.class);
        userExceptAuthor.add(secondUserExceptAuthor);
        AUserInAServer userReacting = Mockito.mock(AUserInAServer.class);
        AUserInAServer starredUser = Mockito.mock(AUserInAServer.class);
        when(starredUser.getUserInServerId()).thenReturn(STARRED_SERVER_USER_ID);
        CachedAuthor cachedAuthor = Mockito.mock(CachedAuthor.class);
        when(cachedAuthor.getAuthorId()).thenReturn(STARRED_USER_ID);
        CachedMessage message = Mockito.mock(CachedMessage.class);
        when(message.getAuthor()).thenReturn(cachedAuthor);
        when(message.getServerId()).thenReturn(SERVER_ID);
        when(message.getChannelId()).thenReturn(CHANNEL_ID);
        Member authorMember = Mockito.mock(Member.class);
        when(botService.getMemberInServerAsync(SERVER_ID, STARRED_USER_ID)).thenReturn(CompletableFuture.completedFuture(authorMember));
        when(botService.getTextChannelFromServerOptional(SERVER_ID, CHANNEL_ID)).thenReturn(Optional.of(mockedTextChannel));
        when(botService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.of(guild));
        ADefaultConfig config = Mockito.mock(ADefaultConfig.class);
        when(config.getLongValue()).thenReturn(3L);
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);
        when(configService.getLongValue("starLvl3", SERVER_ID)).thenReturn(3L);
        when(configService.getLongValue("starLvl2", SERVER_ID)).thenReturn(2L);
        when(emoteService.getUsableEmoteOrDefault(SERVER_ID, "star2")).thenReturn("b");
        when(self.sendStarboardPostAndStore(eq(message), eq(STARRED_SERVER_USER_ID), anyList(), any())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Void> createPostFuture = testUnit.createStarboardPost(message, userExceptAuthor, userReacting, starredUser);
        Assert.assertTrue(createPostFuture.isDone());
    }

    @Test
    public void testSendStarboard() {
        CachedMessage message = Mockito.mock(CachedMessage.class);
        when(message.getServerId()).thenReturn(SERVER_ID);
        StarboardPostModel model = Mockito.mock(StarboardPostModel.class);
        when(templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, model)).thenReturn(messageToSend);
        PostTarget postTarget = Mockito.mock(PostTarget.class);
        when(postTarget.getChannelReference()).thenReturn(starboardChannel);
        when(starboardChannel.getId()).thenReturn(STARBOARD_CHANNEL_ID);
        when(postTargetManagement.getPostTarget(StarboardPostTarget.STARBOARD.getKey(), SERVER_ID)).thenReturn(postTarget);
        when(postTargetService.sendEmbedInPostTarget(messageToSend, StarboardPostTarget.STARBOARD, SERVER_ID)).thenReturn(Arrays.asList(CompletableFuture.completedFuture(null)));
        ArrayList<Long> userExceptAuthorIds = new ArrayList<>();
        testUnit.sendStarboardPostAndStore(message, STARRED_USER_ID, userExceptAuthorIds, model);
        verify(self, times(1)).persistPost(eq(message), eq(userExceptAuthorIds), any(), eq(STARBOARD_CHANNEL_ID), eq(STARRED_USER_ID));
    }

    @Test
    public void testPersistPostWithTwoReactors() {
        AUserInAServer userReacting = Mockito.mock(AUserInAServer.class);
        AUserInAServer starredUser = Mockito.mock(AUserInAServer.class);
        CachedMessage message = Mockito.mock(CachedMessage.class);
        List<Long> userExceptAuthorIds = Arrays.asList(FIRST_USER_IN_SERVER_ID, SECOND_USER_IN_SERVER_ID);
        List<CompletableFuture<Message>> futures = Arrays.asList(CompletableFuture.completedFuture(sendPost));
        when(userInServerManagementService.loadUserOptional(STARRED_SERVER_USER_ID)).thenReturn(Optional.of(starredUser));
        when(userInServerManagementService.loadUserOptional(FIRST_USER_IN_SERVER_ID)).thenReturn(Optional.of(userReacting));
        when(userReacting.getUserInServerId()).thenReturn(FIRST_USER_IN_SERVER_ID);
        AChannel channel = Mockito.mock(AChannel.class);
        when(channelManagementService.loadChannel(CHANNEL_ID)).thenReturn(channel);
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(starboardPostManagementService.createStarboardPost(eq(message), eq(starredUser), any(AServerAChannelMessage.class))).thenReturn(post);
        AUserInAServer secondStarrerUserObj = Mockito.mock(AUserInAServer.class);
        when(userInServerManagementService.loadUserOptional(SECOND_USER_IN_SERVER_ID)).thenReturn(Optional.of(secondStarrerUserObj));
        when(secondStarrerUserObj.getUserInServerId()).thenReturn(SECOND_USER_IN_SERVER_ID);
        testUnit.persistPost(message, userExceptAuthorIds, futures, CHANNEL_ID, STARRED_SERVER_USER_ID);
        verify(starboardPostReactorManagementService, times(2)).addReactor(eq(post), userInAServerArgumentCaptor.capture());
        List<AUserInAServer> addedReactors = userInAServerArgumentCaptor.getAllValues();
        Assert.assertEquals(FIRST_USER_IN_SERVER_ID, addedReactors.get(0).getUserInServerId());
        Assert.assertEquals(SECOND_USER_IN_SERVER_ID, addedReactors.get(1).getUserInServerId());
        Assert.assertEquals(2, addedReactors.size());
    }

    @Test
    public void testUpdateStarboardPost() {
        Long newPostId = 37L;
        Long oldPostId = 36L;
        AChannel sourceChannel = Mockito.mock(AChannel.class);
        when(sourceChannel.getServer()).thenReturn(server);
        CachedMessage message = Mockito.mock(CachedMessage.class);
        when(message.getChannelId()).thenReturn(CHANNEL_ID);
        when(message.getServerId()).thenReturn(SERVER_ID);
        CachedAuthor author = Mockito.mock(CachedAuthor.class);
        when(author.getAuthorId()).thenReturn(STARRED_USER_ID);
        when(message.getAuthor()).thenReturn(author);
        Long starboardPostId = 47L;
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(post.getStarboardMessageId()).thenReturn(oldPostId);
        when(post.getSourceChanel()).thenReturn(sourceChannel);
        when(post.getId()).thenReturn(starboardPostId);
        MessageToSend postMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(STARBOARD_POST_TEMPLATE), starboardPostModelArgumentCaptor.capture())).thenReturn(postMessage);
        when(postTargetService.editOrCreatedInPostTarget(oldPostId, postMessage, StarboardPostTarget.STARBOARD, SERVER_ID)).thenReturn(Arrays.asList(CompletableFuture.completedFuture(sendPost)));
        when(sendPost.getIdLong()).thenReturn(newPostId);
        ADefaultConfig config = Mockito.mock(ADefaultConfig.class);
        when(config.getLongValue()).thenReturn(4L);
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);
        when(starboardPostManagementService.findByStarboardPostId(starboardPostId)).thenReturn(Optional.of(post));
        when(botService.getMemberInServerAsync(SERVER_ID, STARRED_USER_ID)).thenReturn(CompletableFuture.completedFuture(starredMember));
        List<AUserInAServer > userExceptAuthor = new ArrayList<>();
        testUnit.updateStarboardPost(post, message, userExceptAuthor);
        verify(postTargetService, times(1)).editOrCreatedInPostTarget(oldPostId, postMessage, StarboardPostTarget.STARBOARD, SERVER_ID);
        verify(starboardPostManagementService, times(1)).setStarboardPostMessageId(post, newPostId);
    }

    @Test
    public void testDeleteStarboardMessagePost() {
        Long messageId = 4L;
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(post.getStarboardMessageId()).thenReturn(messageId);
        AChannel channel = Mockito.mock(AChannel.class);
        when(channel.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(post.getSourceChanel()).thenReturn(channel);
        when(post.getStarboardChannel()).thenReturn(channel);
        testUnit.deleteStarboardMessagePost(post);
        verify(botService, times(1)).deleteMessage(SERVER_ID, CHANNEL_ID, messageId);
    }

    @Test(expected = UserInServerNotFoundException.class)
    public void testPersistingOfNotFoundStarredUser() {
        when(userInServerManagementService.loadUserOptional(SECOND_USER_IN_SERVER_ID)).thenReturn(Optional.empty());
        CachedMessage message = Mockito.mock(CachedMessage.class);
        List<Long> userExceptAuthorIds = Arrays.asList(FIRST_USER_IN_SERVER_ID);
        List<CompletableFuture<Message>> futures = Arrays.asList(CompletableFuture.completedFuture(sendPost));
        testUnit.persistPost(message, userExceptAuthorIds, futures, CHANNEL_ID, SECOND_USER_IN_SERVER_ID);
    }

    @Test
    public void testRetrieveStarStats() {
        Integer limit = 3;
        AChannel channel = Mockito.mock(AChannel.class);
        when(channel.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        Long firstPostMessageId = 50L;
        Long secondPostMessageId = 51L;
        StarboardPostReaction reaction = Mockito.mock(StarboardPostReaction.class);
        StarboardPost post1 = Mockito.mock(StarboardPost.class);
        when(post1.getReactions()).thenReturn(Arrays.asList(reaction));
        when(post1.getPostMessageId()).thenReturn(firstPostMessageId);
        when(post1.getStarboardChannel()).thenReturn(channel);
        StarboardPost post2 = Mockito.mock(StarboardPost.class);
        when(post2.getPostMessageId()).thenReturn(secondPostMessageId);
        when(post2.getReactions()).thenReturn(new ArrayList<>());
        when(post2.getStarboardChannel()).thenReturn(channel);
        List<StarboardPost> topPosts = Arrays.asList(post1, post2);
        when(starboardPostManagementService.retrieveTopPosts(SERVER_ID, limit)).thenReturn(topPosts);
        CompletableFuture<StarStatsUser> statsUser = CompletableFuture.completedFuture(Mockito.mock(StarStatsUser.class));
        CompletableFuture<StarStatsUser> statsUser2 = CompletableFuture.completedFuture(Mockito.mock(StarStatsUser.class));
        List<CompletableFuture<StarStatsUser>> topGiver = Arrays.asList(statsUser, statsUser2);
        when(starboardPostReactorManagementService.retrieveTopStarGiver(SERVER_ID, limit)).thenReturn(topGiver);
        when(starboardPostReactorManagementService.retrieveTopStarReceiver(SERVER_ID, limit)).thenReturn(topGiver);
        when(starboardPostManagementService.getPostCount(SERVER_ID)).thenReturn(50);
        when(starboardPostReactorManagementService.getStarCount(SERVER_ID)).thenReturn(500);
        when(emoteService.getUsableEmoteOrDefault(SERVER_ID, "starboardBadge1")).thenReturn("1");
        when(emoteService.getUsableEmoteOrDefault(SERVER_ID, "starboardBadge2")).thenReturn("2");
        when(emoteService.getUsableEmoteOrDefault(SERVER_ID, "starboardBadge3")).thenReturn("3");
        CompletableFuture<StarStatsModel> modelFuture = testUnit.retrieveStarStats(SERVER_ID);
        StarStatsModel model = modelFuture.join();
        List<String> badgeEmotes = model.getBadgeEmotes();
        Assert.assertEquals(limit.intValue(), badgeEmotes.size());
        Assert.assertEquals("1", badgeEmotes.get(0));
        Assert.assertEquals("2", badgeEmotes.get(1));
        Assert.assertEquals("3", badgeEmotes.get(2));
        Assert.assertEquals(500, model.getTotalStars().intValue());
        Assert.assertEquals(50, model.getStarredMessages().intValue());
        StarStatsPost topPost = model.getTopPosts().get(0);
        Assert.assertEquals(SERVER_ID, topPost.getServerId());
        Assert.assertEquals(channel.getId(), topPost.getChannelId());
        Assert.assertEquals(firstPostMessageId, topPost.getMessageId());
        Assert.assertEquals(1, topPost.getStarCount().intValue());
        StarStatsPost secondTopPost = model.getTopPosts().get(1);
        Assert.assertEquals(SERVER_ID, secondTopPost.getServerId());
        Assert.assertEquals(channel.getId(), secondTopPost.getChannelId());
        Assert.assertEquals(secondPostMessageId, secondTopPost.getMessageId());
        Assert.assertEquals(0, secondTopPost.getStarCount().intValue());

    }

}
