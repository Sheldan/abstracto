package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.listener.MessageEmbeddedModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.models.MessageEmbedLink;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
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
    private MessageEmbedService self;

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
    public void testEmbeddingLink() {
        Long channelId = 6L;
        Long serverId = 4L;
        AServer server = MockUtils.getServer(serverId);
        AChannel aChannel = MockUtils.getTextChannel(server, channelId);
        Long userEmbeddingUserInServerId = 5L;
        AUserInAServer embeddingUser = MockUtils.getUserObject(userEmbeddingUserInServerId, server);
        Long authorId = 7L;
        AUserInAServer authorUser = MockUtils.getUserObject(authorId, server);
        Long firstMessageId = 6L;
        CachedMessage cachedMessage = CachedMessage.builder().serverId(serverId).channelId(channelId).messageId(firstMessageId).authorId(authorUser.getUserReference().getId()).build();
        Member embeddingMember = Mockito.mock(Member.class);
        Member author = Mockito.mock(Member.class);
        Guild guild = Mockito.mock(Guild.class);
        when(embeddingMessage.getMember()).thenReturn(embeddingMember);
        when(textChannel.getIdLong()).thenReturn(channelId);
        when(embeddingMessage.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(serverId);
        when(embeddingMessage.getChannel()).thenReturn(textChannel);
        when(userInServerManagementService.loadUser(embeddingMember)).thenReturn(embeddingUser);
        when(userInServerManagementService.loadUser(userEmbeddingUserInServerId)).thenReturn(Optional.of(embeddingUser));
        when(channelManagementService.loadChannel(channelId)).thenReturn(Optional.of(aChannel));
        when(serverManagementService.loadOrCreate(serverId)).thenReturn(server);
        when(botService.getMemberInServer(cachedMessage.getServerId(), cachedMessage.getAuthorId())).thenReturn(author);
        when(botService.getTextChannelFromServer(cachedMessage.getServerId(), cachedMessage.getChannelId())).thenReturn(Optional.of(textChannel));
        MessageToSend messageToSend = MessageToSend.builder().build();
        when(templateService.renderEmbedTemplate(eq(MessageEmbedServiceBean.MESSAGE_EMBED_TEMPLATE), any(MessageEmbeddedModel.class))).thenReturn(messageToSend);
        Message messageContainingEmbed = Mockito.mock(Message.class);
        when(channelService.sendMessageToSendToChannel(messageToSend, textChannel)).thenReturn(Arrays.asList(CompletableFuture.completedFuture(messageContainingEmbed)));
        testUnit.embedLink(cachedMessage, textChannel, userEmbeddingUserInServerId, embeddingMessage);
        verify(messageEmbedPostManagementService, times(1)).createMessageEmbed(cachedMessage, messageContainingEmbed, embeddingUser);
        verify(messageService, times(1)).addReactionToMessage(MessageEmbedServiceBean.REMOVAL_EMOTE, cachedMessage.getServerId(), messageContainingEmbed);
    }

    @Test
    public void testNotFoundUserEmbeddingLink() {
        Long channelId = 6L;
        Long serverId = 4L;
        Long firstMessageId = 6L;
        CachedMessage cachedMessage = CachedMessage.builder().serverId(serverId).channelId(channelId).messageId(firstMessageId).build();
        Long userEmbeddingUserInServerId = 5L;
        when(userInServerManagementService.loadUser(userEmbeddingUserInServerId)).thenReturn(Optional.empty());
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
