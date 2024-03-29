package dev.sheldan.abstracto.linkembed.service;

import dev.sheldan.abstracto.core.models.GuildMemberMessageChannel;
import dev.sheldan.abstracto.core.models.cache.CachedAuthor;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureDefinition;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureMode;
import dev.sheldan.abstracto.linkembed.model.template.MessageEmbeddedModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import dev.sheldan.abstracto.linkembed.service.management.MessageEmbedPostManagementService;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageEmbedServiceBeanTest {

    @InjectMocks
    private MessageEmbedServiceBean testUnit;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private MemberService memberService;

    @Mock
    private TemplateService templateService;

    @Mock
    private ChannelService channelService;

    @Mock
    private MessageEmbedServiceBean self;

    @Mock
    private MessageCache messageCache;

    @Mock
    private MessageEmbedPostManagementService messageEmbedPostManagementService;

    @Mock
    private UserService userService;

    @Mock
    private ReactionService reactionService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private GuildMessageChannel textChannel;

    @Mock
    private GuildMemberMessageChannel embeddingMessage;

    @Mock
    private Message message;

    @Mock
    private Guild guild;

    @Mock
    private CachedMessage cachedMessage;

    @Captor
    private ArgumentCaptor<CachedMessage> cachedMessageArgumentCaptor;

    private static final String FIRST_LINK_TEXT = "https://discordapp.com/channels/1/2/3";
    private static final String SHORTER_LINK_TEXT = "https://discord.com/channels/1/2/3";
    private static final String SECOND_LINK_TEXT = "https://discordapp.com/channels/2/3/4";

    private static final Long EMBEDDING_USER_IN_SERVER_ID = 8L;
    private static final Long SERVER_ID = 8L;
    private static final Long CHANNEL_ID = 10L;
    private static final Long USER_ID = 9L;

    @Mock
    private AUserInAServer embeddingUser;

    @Mock
    private Member embeddingMember;

    @Mock
    private Member embeddedMember;

    @Mock
    private User embeddedUser;

    @Test
    public void testNoLinkInString(){
        String message = "test";
        List<MessageEmbedLink> linksInMessage = testUnit.getLinksInMessage(message);
        Assert.assertEquals(0, linksInMessage.size());
    }

    @Test
    public void testDMLinkInString(){
        String message = "https://discordapp.com/channels/@me/1/2";
        List<MessageEmbedLink> linksInMessage = testUnit.getLinksInMessage(message);
        Assert.assertEquals(0, linksInMessage.size());
    }

    @Test
    public void testFindOneLinkInString(){
        List<MessageEmbedLink> linksInMessage = testUnit.getLinksInMessage(FIRST_LINK_TEXT);
        Assert.assertEquals(1, linksInMessage.size());
        MessageEmbedLink firstLink = linksInMessage.get(0);
        Assert.assertEquals(1, firstLink.getServerId().intValue());
        Assert.assertEquals(2, firstLink.getChannelId().intValue());
        Assert.assertEquals(3, firstLink.getMessageId().intValue());
        Assert.assertEquals(FIRST_LINK_TEXT, firstLink.getWholeUrl());
    }

    @Test
    public void testNewShortDomain(){
        List<MessageEmbedLink> linksInMessage = testUnit.getLinksInMessage(SHORTER_LINK_TEXT);
        Assert.assertEquals(1, linksInMessage.size());
        MessageEmbedLink firstLink = linksInMessage.get(0);
        Assert.assertEquals(1, firstLink.getServerId().intValue());
        Assert.assertEquals(2, firstLink.getChannelId().intValue());
        Assert.assertEquals(3, firstLink.getMessageId().intValue());
        Assert.assertEquals(SHORTER_LINK_TEXT, firstLink.getWholeUrl());
    }

    @Test
    public void testTwoLinksInString(){
        String message = String.format("%s %s", FIRST_LINK_TEXT, SECOND_LINK_TEXT);
        executeTestWithTwoLinks(message);
    }

    @Test
    public void testLinksWithTextInBetween(){
        String message = String.format("%s some more text %s", FIRST_LINK_TEXT, SECOND_LINK_TEXT);
        executeTestWithTwoLinks(message);
    }

    @Test
    public void testEmbedNoLinks() {
        testUnit.embedLinks(new ArrayList<>(), textChannel, 5L, embeddingMessage);
        verify(messageCache, times(0)).getMessageFromCache(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testEmbedSingularLink() {
        List<MessageEmbedLink> linksToEmbed = new ArrayList<>();
        Long channelId = 6L;
        Long firstMessageId = 6L;
        MessageEmbedLink messageEmbedLink = Mockito.mock(MessageEmbedLink.class);
        when(messageEmbedLink.getServerId()).thenReturn(SERVER_ID);
        when(messageEmbedLink.getChannelId()).thenReturn(channelId);
        when(messageEmbedLink.getMessageId()).thenReturn(firstMessageId);
        linksToEmbed.add(messageEmbedLink);
        CachedMessage firstCachedMessage = Mockito.mock(CachedMessage.class);
        when(messageCache.getMessageFromCache(SERVER_ID,channelId, firstMessageId)).thenReturn(CompletableFuture.completedFuture(firstCachedMessage));
        Long embeddingUserId = 5L;
        testUnit.embedLinks(linksToEmbed, textChannel, embeddingUserId, embeddingMessage);
        verify( self, times(1)).embedLink(firstCachedMessage, textChannel, embeddingUserId, embeddingMessage);
    }

    @Test
    public void testEmbedMultipleLinks() {
        List<MessageEmbedLink> linksToEmbed = new ArrayList<>();
        Long firstMessageId = 6L;
        Long secondMessageId = 7L;
        MessageEmbedLink messageEmbedLink = mockMessageEmbedLink(firstMessageId);
        linksToEmbed.add(messageEmbedLink);
        MessageEmbedLink secondMessageEmbedLink = mockMessageEmbedLink(secondMessageId);
        linksToEmbed.add(secondMessageEmbedLink);
        CachedMessage firstCachedMessage = mockCachedMessage(firstMessageId);
        CachedMessage secondCacheMessage = mockCachedMessage(secondMessageId);
        when(messageCache.getMessageFromCache(SERVER_ID,CHANNEL_ID, firstMessageId)).thenReturn(CompletableFuture.completedFuture(firstCachedMessage));
        when(messageCache.getMessageFromCache(SERVER_ID,CHANNEL_ID, secondMessageId)).thenReturn(CompletableFuture.completedFuture(secondCacheMessage));
        Long embeddingUserId = 5L;
        testUnit.embedLinks(linksToEmbed, textChannel, embeddingUserId, embeddingMessage);
        verify( self, times(2)).embedLink(cachedMessageArgumentCaptor.capture(), eq(textChannel), eq(embeddingUserId) , eq(embeddingMessage));

        List<CachedMessage> cachedMessages = cachedMessageArgumentCaptor.getAllValues();
        Assert.assertEquals(2, cachedMessages.size());
        CachedMessage firstEmbeddedMessage = cachedMessages.get(0);
        Assert.assertEquals(SERVER_ID, firstEmbeddedMessage.getServerId());
        Assert.assertEquals(CHANNEL_ID, firstEmbeddedMessage.getChannelId());
        Assert.assertEquals(firstMessageId, firstEmbeddedMessage.getMessageId());

        CachedMessage secondEmbeddedMessage = cachedMessages.get(1);
        Assert.assertEquals(SERVER_ID, secondEmbeddedMessage.getServerId());
        Assert.assertEquals(CHANNEL_ID, secondEmbeddedMessage.getChannelId());
        Assert.assertEquals(secondMessageId, secondEmbeddedMessage.getMessageId());
    }

    @Test
    public void testLoadingEmbeddingModel() {
        CachedAuthor cachedAuthor = Mockito.mock(CachedAuthor.class);
        when(cachedAuthor.getAuthorId()).thenReturn(USER_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(userService.retrieveUserForId(USER_ID)).thenReturn(CompletableFuture.completedFuture(embeddedUser));
        MessageEmbeddedModel model = Mockito.mock(MessageEmbeddedModel.class);
        when(self.loadMessageEmbedModel(embeddingMessage, cachedMessage, embeddedUser, false)).thenReturn(model);
        when(self.sendEmbeddingMessage(cachedMessage, textChannel, EMBEDDING_USER_IN_SERVER_ID, model, false)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Void> embedFuture = testUnit.embedLink(cachedMessage, textChannel, EMBEDDING_USER_IN_SERVER_ID, embeddingMessage);
        Assert.assertTrue(embedFuture.isDone());
    }

    @Test
    public void testNotFoundUserEmbeddingLink() {
        Long firstMessageId = 6L;
        CachedMessage cachedMessage = mockCachedMessage(firstMessageId);
        Long userEmbeddingUserInServerId = 5L;
        CachedAuthor cachedAuthor = Mockito.mock(CachedAuthor.class);
        when(cachedAuthor.getAuthorId()).thenReturn(USER_ID);
        when(cachedMessage.getAuthor()).thenReturn(cachedAuthor);
        when(textChannel.getGuild()).thenReturn(guild);
        when(featureModeService.featureModeActive(LinkEmbedFeatureDefinition.LINK_EMBEDS, guild, LinkEmbedFeatureMode.DELETE_BUTTON)).thenReturn(false);
        when(userService.retrieveUserForId(USER_ID)).thenReturn(CompletableFuture.completedFuture(embeddedUser));
        testUnit.embedLink(cachedMessage, textChannel, userEmbeddingUserInServerId, embeddingMessage);
        verify(messageCache, times(0)).getMessageFromCache(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testSendEmbeddingMessage() {
        MessageEmbeddedModel embeddedModel = Mockito.mock(MessageEmbeddedModel.class);
        MessageToSend messageToSend = Mockito.mock(MessageToSend.class);
        when(textChannel.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(templateService.renderEmbedTemplate(MessageEmbedServiceBean.MESSAGE_EMBED_TEMPLATE, embeddedModel, SERVER_ID)).thenReturn(messageToSend);
        AUser user = Mockito.mock(AUser.class);
        when(embeddingUser.getUserReference()).thenReturn(user);
        when(userInServerManagementService.loadOrCreateUser(EMBEDDING_USER_IN_SERVER_ID)).thenReturn(embeddingUser);
        List<CompletableFuture<Message>> messageFutures = CommandTestUtilities.messageFutureList();
        when(channelService.sendMessageToSendToChannel(messageToSend, textChannel)).thenReturn(messageFutures);
        Message createdMessage = messageFutures.get(0).join();
        when(self.addDeletionPossibility(cachedMessage, embeddedModel, createdMessage, 0L, false)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Void> future = testUnit.sendEmbeddingMessage(cachedMessage, textChannel, EMBEDDING_USER_IN_SERVER_ID, embeddedModel, false);
        Assert.assertFalse(future.isCompletedExceptionally());
    }

    @Test
    public void testLoadUserAndPersistMessage() {
        when(userInServerManagementService.loadOrCreateUser(EMBEDDING_USER_IN_SERVER_ID)).thenReturn(embeddingUser);
        testUnit.loadUserAndPersistMessage(cachedMessage, EMBEDDING_USER_IN_SERVER_ID, message, null);
        verify(messageEmbedPostManagementService, times(1)).createMessageEmbed(cachedMessage, message, embeddingUser, null);
    }

    @Test
    public void testLoadMessageEmbedModel() {
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getChannelId()).thenReturn(CHANNEL_ID);
        when(channelService.getMessageChannelFromServerOptional(SERVER_ID, CHANNEL_ID)).thenReturn(Optional.of(textChannel));
        when(embeddingMessage.getGuild()).thenReturn(guild);
        when(embeddingMessage.getGuildChannel()).thenReturn(textChannel);
        when(embeddingMessage.getMember()).thenReturn(embeddingMember);
        MessageEmbeddedModel createdModel = testUnit.loadMessageEmbedModel(embeddingMessage, cachedMessage, embeddedUser, false);
        Assert.assertEquals(textChannel, createdModel.getSourceChannel());
        Assert.assertEquals(guild, createdModel.getGuild());
        Assert.assertEquals(textChannel, createdModel.getMessageChannel());
        Assert.assertEquals(embeddedUser, createdModel.getAuthor());
        Assert.assertEquals(embeddingMember, createdModel.getMember());
        Assert.assertEquals(embeddingMember, createdModel.getEmbeddingUser());
        Assert.assertEquals(cachedMessage, createdModel.getEmbeddedMessage());
    }
    private void executeTestWithTwoLinks(String message) {
        List<MessageEmbedLink> linksInMessage = testUnit.getLinksInMessage(message);
        Assert.assertEquals(2, linksInMessage.size());
        MessageEmbedLink firstLink = linksInMessage.get(0);
        Assert.assertEquals(1, firstLink.getServerId().intValue());
        Assert.assertEquals(2, firstLink.getChannelId().intValue());
        Assert.assertEquals(3, firstLink.getMessageId().intValue());

        MessageEmbedLink secondLink = linksInMessage.get(1);
        Assert.assertEquals(2, secondLink.getServerId().intValue());
        Assert.assertEquals(3, secondLink.getChannelId().intValue());
        Assert.assertEquals(4, secondLink.getMessageId().intValue());
    }

    private MessageEmbedLink mockMessageEmbedLink(Long messageId) {
        MessageEmbedLink secondMessageEmbedLink = Mockito.mock(MessageEmbedLink.class);
        when(secondMessageEmbedLink.getServerId()).thenReturn(SERVER_ID);
        when(secondMessageEmbedLink.getChannelId()).thenReturn(CHANNEL_ID);
        when(secondMessageEmbedLink.getMessageId()).thenReturn(messageId);
        return secondMessageEmbedLink;
    }

    private CachedMessage mockCachedMessage(Long secondMessageId) {
        CachedMessage secondCacheMessage = Mockito.mock(CachedMessage.class);
        when(secondCacheMessage.getServerId()).thenReturn(SERVER_ID);
        when(secondCacheMessage.getChannelId()).thenReturn(CHANNEL_ID);
        when(secondCacheMessage.getMessageId()).thenReturn(secondMessageId);
        return secondCacheMessage;
    }
}
