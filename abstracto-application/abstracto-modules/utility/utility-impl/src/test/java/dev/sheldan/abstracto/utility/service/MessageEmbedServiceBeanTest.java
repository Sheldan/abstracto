package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.listener.MessageEmbeddedModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.models.MessageEmbedLink;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
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
    private BotService botService;

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
    private MessageService messageService;

    @Mock
    private TextChannel textChannel;

    @Mock
    private Message embeddingMessage;

    @Captor
    private ArgumentCaptor<CachedMessage> cachedMessageArgumentCaptor;

    private static final String FIRST_LINK_TEXT = "https://discordapp.com/channels/1/2/3";
    private static final String SHORTER_LINK_TEXT = "https://discord.com/channels/1/2/3";
    private static final String SECOND_LINK_TEXT = "https://discordapp.com/channels/2/3/4";

    private static final Long EMBEDDING_USER_IN_SERVER_ID = 8L;
    private static final Long SERVER_ID = 8L;
    private static final Long USER_ID = 9L;

    @Mock
    private AUserInAServer embeddingUser;

    @Mock
    private Member embeddingMember;

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
    }

    @Test
    public void testEmbedSingularLink() {
        List<MessageEmbedLink> linksToEmbed = new ArrayList<>();
        Long channelId = 6L;
        Long serverId = 4L;
        Long firstMessageId = 6L;
        linksToEmbed.add(MessageEmbedLink.builder().serverId(serverId).channelId(channelId).messageId(firstMessageId).build());
        CachedMessage firstCachedMessage = CachedMessage.builder().serverId(serverId).channelId(channelId).messageId(firstMessageId).build();
        when(messageCache.getMessageFromCache(serverId,channelId, firstMessageId)).thenReturn(CompletableFuture.completedFuture(firstCachedMessage));
        Long embeddingUserId = 5L;
        testUnit.embedLinks(linksToEmbed, textChannel, embeddingUserId, embeddingMessage);
        verify( self, times(1)).embedLink(eq(firstCachedMessage), eq(textChannel), eq(embeddingUserId) , eq(embeddingMessage));
    }

    @Test
    public void testEmbedMultipleLinks() {
        List<MessageEmbedLink> linksToEmbed = new ArrayList<>();
        Long channelId = 6L;
        Long serverId = 4L;
        Long firstMessageId = 6L;
        Long secondMessageId = 7L;
        linksToEmbed.add(MessageEmbedLink.builder().serverId(serverId).channelId(channelId).messageId(firstMessageId).build());
        linksToEmbed.add(MessageEmbedLink.builder().serverId(serverId).channelId(channelId).messageId(secondMessageId).build());
        CachedMessage firstCachedMessage = CachedMessage.builder().serverId(serverId).channelId(channelId).messageId(firstMessageId).build();
        CachedMessage secondCacheMessage = CachedMessage.builder().serverId(serverId).channelId(channelId).messageId(secondMessageId).build();
        when(messageCache.getMessageFromCache(serverId,channelId, firstMessageId)).thenReturn(CompletableFuture.completedFuture(firstCachedMessage));
        when(messageCache.getMessageFromCache(serverId,channelId, secondMessageId)).thenReturn(CompletableFuture.completedFuture(secondCacheMessage));
        Long embeddingUserId = 5L;
        testUnit.embedLinks(linksToEmbed, textChannel, embeddingUserId, embeddingMessage);
        verify( self, times(2)).embedLink(cachedMessageArgumentCaptor.capture(), eq(textChannel), eq(embeddingUserId) , eq(embeddingMessage));

        List<CachedMessage> cachedMessages = cachedMessageArgumentCaptor.getAllValues();
        Assert.assertEquals(2, cachedMessages.size());
        CachedMessage firstEmbeddedMessage = cachedMessages.get(0);
        Assert.assertEquals(serverId, firstEmbeddedMessage.getServerId());
        Assert.assertEquals(channelId, firstEmbeddedMessage.getChannelId());
        Assert.assertEquals(firstMessageId, firstEmbeddedMessage.getMessageId());

        CachedMessage secondEmbeddedMessage = cachedMessages.get(1);
        Assert.assertEquals(serverId, secondEmbeddedMessage.getServerId());
        Assert.assertEquals(channelId, secondEmbeddedMessage.getChannelId());
        Assert.assertEquals(secondMessageId, secondEmbeddedMessage.getMessageId());
    }

    @Test
    public void testLoadingEmbeddingModel() {
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(cachedMessage.getServerId()).thenReturn(SERVER_ID);
        when(cachedMessage.getAuthorId()).thenReturn(USER_ID);
        when(userInServerManagementService.loadUserConditional(EMBEDDING_USER_IN_SERVER_ID)).thenReturn(Optional.of(embeddingUser));
        when(botService.getMemberInServerAsync(SERVER_ID, USER_ID)).thenReturn(CompletableFuture.completedFuture(embeddingMember));
        MessageEmbeddedModel model = Mockito.mock(MessageEmbeddedModel.class);
        when(self.loadMessageEmbedModel(embeddingMessage, cachedMessage, embeddingMember)).thenReturn(model);
        when(self.sendEmbeddingMessage(cachedMessage, textChannel, EMBEDDING_USER_IN_SERVER_ID, model)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.embedLink(cachedMessage, textChannel, EMBEDDING_USER_IN_SERVER_ID, embeddingMessage).join();
    }

    @Test
    public void testNotFoundUserEmbeddingLink() {
        Long channelId = 6L;
        Long serverId = 4L;
        Long firstMessageId = 6L;
        CachedMessage cachedMessage = CachedMessage.builder().serverId(serverId).channelId(channelId).messageId(firstMessageId).build();
        Long userEmbeddingUserInServerId = 5L;
        when(userInServerManagementService.loadUserConditional(userEmbeddingUserInServerId)).thenReturn(Optional.empty());
        testUnit.embedLink(cachedMessage, textChannel, userEmbeddingUserInServerId, embeddingMessage);
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
}
