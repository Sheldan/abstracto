package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
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
import dev.sheldan.abstracto.test.MockUtils;
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

    private static final Long STARRED_USER_ID = 5L;
    private static final Long SERVER_ID = 6L;
    private static final Long STARBOARD_CHANNEL_ID = 8L;

    @Captor
    private ArgumentCaptor<AUserInAServer> userInAServerArgumentCaptor;

    @Captor
    private ArgumentCaptor<StarboardPostModel> starboardPostModelArgumentCaptor;

    @Test
    public void testCreateStarboardPost() {
        AServer server = MockUtils.getServer();
        List<AUserInAServer > userExceptAuthor = new ArrayList<>();
        userExceptAuthor.add(MockUtils.getUserObject(2L, server));
        userExceptAuthor.add(MockUtils.getUserObject(10L, server));
        AUserInAServer userReacting = MockUtils.getUserObject(4L, server);
        AUserInAServer starredUser = MockUtils.getUserObject(5L, server);
        Long channelId = 10L;
        Long starredUserId = starredUser.getUserReference().getId();
        CachedMessage message = CachedMessage
                .builder()
                .authorId(starredUserId)
                .serverId(server.getId())
                .channelId(channelId)
                .build();
        Member authorMember = Mockito.mock(Member.class);
        when(botService.getMemberInServerAsync(message.getServerId(), message.getAuthorId())).thenReturn(CompletableFuture.completedFuture(authorMember));
        when(botService.getTextChannelFromServerOptional(server.getId(), channelId)).thenReturn(Optional.of(mockedTextChannel));
        when(botService.getGuildByIdOptional(server.getId())).thenReturn(Optional.of(guild));
        ADefaultConfig config = ADefaultConfig.builder().longValue(3L).build();
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);
        when(configService.getLongValue("starLvl3", server.getId())).thenReturn(3L);
        when(configService.getLongValue("starLvl2", server.getId())).thenReturn(2L);
        when(emoteService.getUsableEmoteOrDefault(server.getId(), "star2")).thenReturn("b");
        when(self.sendStarboardPostAndStore(eq(message), eq(starredUserId), anyList(), any())).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.createStarboardPost(message, userExceptAuthor, userReacting, starredUser).join();
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
    public void testPersistPost() {
        AServer server = MockUtils.getServer();
        AUserInAServer userReacting = MockUtils.getUserObject(4L, server);
        AUserInAServer starredUser = MockUtils.getUserObject(5L, server);
        Long channelId = 10L;
        CachedMessage message = CachedMessage
                .builder()
                .authorId(starredUser.getUserReference().getId())
                .serverId(server.getId())
                .channelId(channelId)
                .build();
        Long secondStarrerUserId = 2L;
        List<Long> userExceptAuthorIds = Arrays.asList(secondStarrerUserId, userReacting.getUserReference().getId());
        List<CompletableFuture<Message>> futures = Arrays.asList(CompletableFuture.completedFuture(sendPost));
        when(userInServerManagementService.loadUserConditional(starredUser.getUserInServerId())).thenReturn(Optional.of(starredUser));
        when(userInServerManagementService.loadUserConditional(userReacting.getUserInServerId())).thenReturn(Optional.of(userReacting));
        AChannel channel = MockUtils.getTextChannel(server, channelId);
        when(channelManagementService.loadChannel(channelId)).thenReturn(channel);
        StarboardPost post = StarboardPost.builder().build();
        when(starboardPostManagementService.createStarboardPost(eq(message), eq(starredUser), any(AServerAChannelMessage.class))).thenReturn(post);
        AUserInAServer secondStarrerUserObj = MockUtils.getUserObject(secondStarrerUserId, server);
        when(userInServerManagementService.loadUserConditional(secondStarrerUserId)).thenReturn(Optional.of(secondStarrerUserObj));
        when(userInServerManagementService.loadUserConditional(userReacting.getUserInServerId())).thenReturn(Optional.of(userReacting));
        testUnit.persistPost(message, userExceptAuthorIds, futures, channelId, starredUser.getUserInServerId());
        verify(starboardPostReactorManagementService, times(2)).addReactor(eq(post), userInAServerArgumentCaptor.capture());
        List<AUserInAServer> addedReactors = userInAServerArgumentCaptor.getAllValues();
        Assert.assertEquals(secondStarrerUserId, addedReactors.get(0).getUserInServerId());
        Assert.assertEquals(userReacting.getUserInServerId(), addedReactors.get(1).getUserInServerId());
        Assert.assertEquals(2, addedReactors.size());
    }

    @Test
    public void testUpdateStarboardPost() {
        AServer server = MockUtils.getServer();
        Long postMessageId = 25L;
        Long newPostId= 37L;
        Long oldPostId = 36L;
        AUserInAServer starredUser = MockUtils.getUserObject(5L, server);
        Long channelId = 10L;
        AChannel sourceChannel = Mockito.mock(AChannel.class);
        when(sourceChannel.getServer()).thenReturn(server);
        CachedMessage message = CachedMessage
                .builder()
                .authorId(starredUser.getUserReference().getId())
                .serverId(server.getId())
                .channelId(channelId)
                .build();
        Long starboardPostId = 47L;
        StarboardPost post = StarboardPost.builder().postMessageId(postMessageId).starboardMessageId(oldPostId).sourceChanel(sourceChannel).id(starboardPostId).build();
        MessageToSend postMessage = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(STARBOARD_POST_TEMPLATE), starboardPostModelArgumentCaptor.capture())).thenReturn(postMessage);
        when(postTargetService.editOrCreatedInPostTarget(oldPostId, postMessage, StarboardPostTarget.STARBOARD, server.getId())).thenReturn(Arrays.asList(CompletableFuture.completedFuture(sendPost)));
        when(sendPost.getIdLong()).thenReturn(newPostId);
        ADefaultConfig config = ADefaultConfig.builder().longValue(4L).build();
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);
        when(starboardPostManagementService.findByStarboardPostId(starboardPostId)).thenReturn(Optional.of(post));
        when(botService.getMemberInServerAsync(server.getId(), starredUser.getUserReference().getId())).thenReturn(CompletableFuture.completedFuture(starredMember));
        List<AUserInAServer > userExceptAuthor = new ArrayList<>();
        testUnit.updateStarboardPost(post, message, userExceptAuthor);
        verify(postTargetService, times(1)).editOrCreatedInPostTarget(oldPostId, postMessage, StarboardPostTarget.STARBOARD, server.getId());
        verify(starboardPostManagementService, times(1)).setStarboardPostMessageId(post, newPostId);
    }

    @Test
    public void testDeleteStarboardMessagePost() {
        AServer server = MockUtils.getServer();
        AChannel channel = MockUtils.getTextChannel(server, 4L);
        Long messageId = 4L;
        StarboardPost post = StarboardPost
                .builder()
                .starboardChannel(channel)
                .sourceChanel(channel)
                .starboardMessageId(messageId)
                .build();
        testUnit.deleteStarboardMessagePost(post);
        verify(botService, times(1)).deleteMessage(server.getId(), channel.getId(), messageId);
    }

    @Test(expected = UserInServerNotFoundException.class)
    public void testPersistingOfNotFoundStarredUser() {
        AServer server = MockUtils.getServer();
        AUserInAServer userReacting = MockUtils.getUserObject(4L, server);
        AUserInAServer starredUser = MockUtils.getUserObject(5L, server);
        when(userInServerManagementService.loadUserConditional(starredUser.getUserInServerId())).thenReturn(Optional.empty());
        executeLoadErrorTest(server, userReacting, starredUser, 10L);
    }

    @Test
    public void testRetrieveStarStats() {
        AServer server = MockUtils.getServer();
        Integer limit = 3;
        AChannel channel = MockUtils.getTextChannel(server, 4L);
        Long firstPostMessageId = 50L;
        Long secondPostMessageId = 51L;
        StarboardPostReaction reaction = StarboardPostReaction.builder().build();
        StarboardPost post1 = StarboardPost.builder().starboardChannel(channel).postMessageId(firstPostMessageId).reactions(Arrays.asList(reaction)).build();
        StarboardPost post2 = StarboardPost.builder().starboardChannel(channel).postMessageId(secondPostMessageId).reactions(new ArrayList<>()).build();
        List<StarboardPost> topPosts = Arrays.asList(post1, post2);
        when(starboardPostManagementService.retrieveTopPosts(server.getId(), limit)).thenReturn(topPosts);
        CompletableFuture<StarStatsUser> statsUser = CompletableFuture.completedFuture(Mockito.mock(StarStatsUser.class));
        CompletableFuture<StarStatsUser> statsUser2 = CompletableFuture.completedFuture(Mockito.mock(StarStatsUser.class));
        List<CompletableFuture<StarStatsUser>> topGiver = Arrays.asList(statsUser, statsUser2);
        when(starboardPostReactorManagementService.retrieveTopStarGiver(server.getId(), limit)).thenReturn(topGiver);
        when(starboardPostReactorManagementService.retrieveTopStarReceiver(server.getId(), limit)).thenReturn(topGiver);
        when(starboardPostManagementService.getPostCount(server.getId())).thenReturn(50);
        when(starboardPostReactorManagementService.getStarCount(server.getId())).thenReturn(500);
        when(emoteService.getUsableEmoteOrDefault(server.getId(), "starboardBadge1")).thenReturn("1");
        when(emoteService.getUsableEmoteOrDefault(server.getId(), "starboardBadge2")).thenReturn("2");
        when(emoteService.getUsableEmoteOrDefault(server.getId(), "starboardBadge3")).thenReturn("3");
        CompletableFuture<StarStatsModel> modelFuture = testUnit.retrieveStarStats(server.getId());
        StarStatsModel model = modelFuture.join();
        List<String> badgeEmotes = model.getBadgeEmotes();
        Assert.assertEquals(limit.intValue(), badgeEmotes.size());
        Assert.assertEquals("1", badgeEmotes.get(0));
        Assert.assertEquals("2", badgeEmotes.get(1));
        Assert.assertEquals("3", badgeEmotes.get(2));
        Assert.assertEquals(500, model.getTotalStars().intValue());
        Assert.assertEquals(50, model.getStarredMessages().intValue());
        StarStatsPost topPost = model.getTopPosts().get(0);
        Assert.assertEquals(server.getId(), topPost.getServerId());
        Assert.assertEquals(channel.getId(), topPost.getChannelId());
        Assert.assertEquals(firstPostMessageId, topPost.getMessageId());
        Assert.assertEquals(1, topPost.getStarCount().intValue());
        StarStatsPost secondTopPost = model.getTopPosts().get(1);
        Assert.assertEquals(server.getId(), secondTopPost.getServerId());
        Assert.assertEquals(channel.getId(), secondTopPost.getChannelId());
        Assert.assertEquals(secondPostMessageId, secondTopPost.getMessageId());
        Assert.assertEquals(0, secondTopPost.getStarCount().intValue());

    }

    private void executeLoadErrorTest(AServer server, AUserInAServer userReacting, AUserInAServer starredUser, Long channelId) {
        CachedMessage message = CachedMessage
                .builder()
                .authorId(starredUser.getUserReference().getId())
                .serverId(server.getId())
                .channelId(channelId)
                .build();
        Long secondStarrerUserId = 2L;
        List<Long> userExceptAuthorIds = Arrays.asList(secondStarrerUserId, userReacting.getUserReference().getId());
        List<CompletableFuture<Message>> futures = Arrays.asList(CompletableFuture.completedFuture(sendPost));
        testUnit.persistPost(message, userExceptAuthorIds, futures, channelId, starredUser.getUserInServerId());
    }
}
