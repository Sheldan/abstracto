package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.features.StarboardFeature;
import dev.sheldan.abstracto.utility.config.posttargets.StarboardPostTarget;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.database.StarboardPostReaction;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.*;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostReactorManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.entities.GuildImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.utility.config.features.StarboardFeature.STAR_EMOTE_PREFIX;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardServiceBeanTest {

    @InjectMocks
    private StarboardServiceBean testUnit;

    @Mock
    private GuildService guildService;

    @Mock
    private ChannelService channelService;

    @Mock
    private MemberService memberService;

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
    private MessageService messageService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private StarboardServiceBean self;

    @Mock
    private Guild guild;

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
    private static final Long MESSAGE_ID = 11L;
    private static final Long SECOND_MESSAGE_ID = 12L;

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
        when(memberService.getMemberInServerAsync(SERVER_ID, STARRED_USER_ID)).thenReturn(CompletableFuture.completedFuture(authorMember));
        when(channelService.getTextChannelFromServerOptional(SERVER_ID, CHANNEL_ID)).thenReturn(Optional.of(mockedTextChannel));
        when(guildService.getGuildByIdOptional(SERVER_ID)).thenReturn(Optional.of(guild));
        SystemConfigProperty config = Mockito.mock(SystemConfigProperty.class);
        Long defaultValue = 3L;
        when(config.getLongValue()).thenReturn(defaultValue);
        when(defaultConfigManagementService.getDefaultConfig(StarboardFeature.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);
        when(configService.getLongValue(StarboardFeature.STAR_LVL_CONFIG_PREFIX + "2", SERVER_ID, defaultValue)).thenReturn(2L);
        when(configService.getLongValue(StarboardFeature.STAR_LVL_CONFIG_PREFIX + "3", SERVER_ID, defaultValue)).thenReturn(3L);
        when(defaultConfigManagementService.getDefaultConfig(StarboardFeature.STAR_LVL_CONFIG_PREFIX + "2")).thenReturn(config);
        when(defaultConfigManagementService.getDefaultConfig(StarboardFeature.STAR_LVL_CONFIG_PREFIX + "3")).thenReturn(config);
        when(emoteService.getUsableEmoteOrDefault(SERVER_ID, STAR_EMOTE_PREFIX + "2")).thenReturn("b");
        when(self.sendStarboardPostAndStore(eq(message), eq(STARRED_SERVER_USER_ID), anyList(), any())).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Void> createPostFuture = testUnit.createStarboardPost(message, userExceptAuthor, userReacting, starredUser);
        createPostFuture.join();
        Assert.assertFalse(createPostFuture.isCompletedExceptionally());
    }

    @Test
    public void testSendStarboard() {
        CachedMessage message = Mockito.mock(CachedMessage.class);
        when(message.getServerId()).thenReturn(SERVER_ID);
        StarboardPostModel model = Mockito.mock(StarboardPostModel.class);
        when(templateService.renderEmbedTemplate(StarboardServiceBean.STARBOARD_POST_TEMPLATE, model)).thenReturn(messageToSend);
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
        when(post.getSourceChannel()).thenReturn(sourceChannel);
        when(post.getId()).thenReturn(starboardPostId);
        MessageToSend postMessage = Mockito.mock(MessageToSend.class);
        when(templateService.renderEmbedTemplate(eq(StarboardServiceBean.STARBOARD_POST_TEMPLATE), starboardPostModelArgumentCaptor.capture())).thenReturn(postMessage);
        when(postTargetService.editOrCreatedInPostTarget(oldPostId, postMessage, StarboardPostTarget.STARBOARD, SERVER_ID)).thenReturn(Arrays.asList(CompletableFuture.completedFuture(sendPost)));
        when(sendPost.getIdLong()).thenReturn(newPostId);
        SystemConfigProperty config = Mockito.mock(SystemConfigProperty.class);
        when(config.getLongValue()).thenReturn(1L);
        when(defaultConfigManagementService.getDefaultConfig(StarboardFeature.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);
        when(defaultConfigManagementService.getDefaultConfig(StarboardFeature.STAR_LVL_CONFIG_PREFIX + 1)).thenReturn(config);
        when(starboardPostManagementService.findByStarboardPostId(starboardPostId)).thenReturn(Optional.of(post));
        when(memberService.getMemberInServerAsync(SERVER_ID, STARRED_USER_ID)).thenReturn(CompletableFuture.completedFuture(starredMember));
        List<AUserInAServer > userExceptAuthor = new ArrayList<>();
        CompletableFuture<Void> future = testUnit.updateStarboardPost(post, message, userExceptAuthor);
        future.join();
        Assert.assertFalse(future.isCompletedExceptionally());
        verify(postTargetService, times(1)).editOrCreatedInPostTarget(oldPostId, postMessage, StarboardPostTarget.STARBOARD, SERVER_ID);
        verify(starboardPostManagementService, times(1)).setStarboardPostMessageId(post, newPostId);
    }

    @Test
    public void testDeleteStarboardMessagePost() {
        StarboardPost post = Mockito.mock(StarboardPost.class);
        when(post.getStarboardMessageId()).thenReturn(MESSAGE_ID);
        AChannel channel = Mockito.mock(AChannel.class);
        when(channel.getServer()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(channel.getId()).thenReturn(CHANNEL_ID);
        when(post.getSourceChannel()).thenReturn(channel);
        when(post.getStarboardChannel()).thenReturn(channel);
        testUnit.deleteStarboardMessagePost(post);
        verify(messageService, times(1)).deleteMessageInChannelInServer(SERVER_ID, CHANNEL_ID, MESSAGE_ID);
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
        when(server.getId()).thenReturn(SERVER_ID);
        StarboardPostReaction reaction = Mockito.mock(StarboardPostReaction.class);
        StarboardPost post1 = Mockito.mock(StarboardPost.class);
        when(post1.getReactions()).thenReturn(Arrays.asList(reaction));
        when(post1.getPostMessageId()).thenReturn(MESSAGE_ID);
        when(post1.getServer()).thenReturn(server);
        when(post1.getStarboardChannel()).thenReturn(channel);
        StarboardPost post2 = Mockito.mock(StarboardPost.class);
        when(post2.getPostMessageId()).thenReturn(SECOND_MESSAGE_ID);
        when(post2.getReactions()).thenReturn(new ArrayList<>());
        when(post2.getServer()).thenReturn(server);
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
        CompletableFuture<GuildStarStatsModel> modelFuture = testUnit.retrieveStarStats(SERVER_ID);
        GuildStarStatsModel model = modelFuture.join();
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
        Assert.assertEquals(MESSAGE_ID, topPost.getMessageId());
        Assert.assertEquals(1, topPost.getStarCount().intValue());
        StarStatsPost secondTopPost = model.getTopPosts().get(1);
        Assert.assertEquals(SERVER_ID, secondTopPost.getServerId());
        Assert.assertEquals(channel.getId(), secondTopPost.getChannelId());
        Assert.assertEquals(SECOND_MESSAGE_ID, secondTopPost.getMessageId());
        Assert.assertEquals(0, secondTopPost.getStarCount().intValue());
    }

    @Test
    public void testRetrieveStarStatsForMember() {
        when(starredMember.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(starredMember.getIdLong()).thenReturn(STARRED_USER_ID);
        Long receivedStars = 3L;
        Long givenStars = 3L;
        when(starboardPostManagementService.retrieveReceivedStarsOfUserInServer(SERVER_ID, STARRED_USER_ID)).thenReturn(receivedStars);
        when(starboardPostManagementService.retrieveGivenStarsOfUserInServer(SERVER_ID, STARRED_USER_ID)).thenReturn(givenStars);
        StarboardPost post = Mockito.mock(StarboardPost.class);
        AChannel starboardChannel = Mockito.mock(AChannel.class);
        when(post.getStarboardChannel()).thenReturn(starboardChannel);
        AServer server = Mockito.mock(AServer.class);
        when(server.getId()).thenReturn(SERVER_ID);
        when(post.getServer()).thenReturn(server);
        StarboardPostReaction reaction = Mockito.mock(StarboardPostReaction.class);
        when(post.getReactions()).thenReturn(Collections.singletonList(reaction));
        when(starboardChannel.getId()).thenReturn(STARBOARD_CHANNEL_ID);
        when(post.getPostMessageId()).thenReturn(MESSAGE_ID);
        when(starboardPostManagementService.retrieveTopPostsForUserInServer(SERVER_ID, STARRED_USER_ID, 3)).thenReturn(Collections.singletonList(post));
        MemberStarStatsModel returnedModel = testUnit.retrieveStarStatsForMember(starredMember);
        Assert.assertEquals(receivedStars, returnedModel.getReceivedStars());
        Assert.assertEquals(givenStars, returnedModel.getGivenStars());
        Assert.assertEquals(starredMember, returnedModel.getMember());
        Assert.assertEquals(3, returnedModel.getBadgeEmotes().size());
        Assert.assertEquals(1, returnedModel.getTopPosts().size());
        StarStatsPost starStatsPost = returnedModel.getTopPosts().get(0);
        Assert.assertEquals(STARBOARD_CHANNEL_ID, starStatsPost.getChannelId());
        Assert.assertEquals(SERVER_ID, starStatsPost.getServerId());
        Assert.assertEquals(1, starStatsPost.getStarCount().longValue());
        Assert.assertEquals(MESSAGE_ID, starStatsPost.getMessageId());
    }

}
