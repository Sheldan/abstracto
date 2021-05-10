package dev.sheldan.abstracto.linkembed.listener;

import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import dev.sheldan.abstracto.linkembed.service.MessageEmbedService;
import net.dv8tion.jda.api.entities.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageEmbedListenerTest {

    @InjectMocks
    private MessageEmbedListener testUnit;

    @Mock
    private MessageCache messageCache;

    @Mock
    private MetricService metricService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private MessageEmbedService messageEmbedService;

    @Mock
    private MessageService messageService;

    @Mock
    private MessageEmbedListener self;

    @Mock
    private Message message;

    @Mock
    private TextChannel textChannel;

    @Mock
    private MessageReceivedModel model;

    @Mock
    private Guild guild;

    private static final Long FIRST_SERVER_ID = 12L;
    private static final Long SECOND_SERVER_ID = 13L;
    private static final Long FIRST_CHANNEL_ID = 45L;
    private static final Long USER_IN_SERVER_ID = 1L;
    private static final Long FIRST_MESSAGE_ID = 2L;
    private static final Long SECOND_MESSAGE_ID = 3L;

    @Before
    public void setup(){
        when(guild.getIdLong()).thenReturn(FIRST_SERVER_ID);
        when(message.getGuild()).thenReturn(guild);
        when(message.getChannel()).thenReturn(textChannel);
    }

    @Test
    public void testNoLinkFoundExecution() {
        String text = "text";
        when(message.getContentRaw()).thenReturn(text);
        setupMessageConfig();
        List<MessageEmbedLink> foundMessageLinks = new ArrayList<>();
        when(messageEmbedService.getLinksInMessage(text)).thenReturn(foundMessageLinks);
        when(model.getMessage()).thenReturn(message);
        testUnit.execute(model);
        verify(messageService, times(0)).deleteMessage(message);
    }

    private void setupMessageConfig() {
        when(message.isFromGuild()).thenReturn(true);
        when(message.isWebhookMessage()).thenReturn(false);
        MessageType type = MessageType.DEFAULT;
        when(message.getType()).thenReturn(type);
    }

    @Test
    public void testOnlyOneLinkFoundExecution() {
        String linkText = "link";
        String text = linkText;
        executeLinkEmbedTest(linkText, text);
        verify(self, times(1)).embedSingleLink(eq(message), anyLong(), any(CachedMessage.class));
        verify(messageService, times(1)).deleteMessage(message);
    }

    @Test
    public void testOneLinkWithAdditionalTextExecution() {
        String linkText = "link";
        String text = linkText + "more text";
        executeLinkEmbedTest(linkText, text);
        verify(self, times(1)).embedSingleLink(eq(message), anyLong(), any(CachedMessage.class));
        verify(messageService, times(0)).deleteMessage(message);
    }

    @Test
    public void testLinkFromDifferentServer() {
        String linkText = "link";
        String text = linkText + "more text";
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        when(message.getContentRaw()).thenReturn(text);
        setupMessageConfig();
        MessageEmbedLink foundLink = Mockito.mock(MessageEmbedLink.class);
        when(foundLink.getMessageId()).thenReturn(FIRST_MESSAGE_ID);
        when(foundLink.getServerId()).thenReturn(SECOND_SERVER_ID);
        List<MessageEmbedLink> foundMessageLinks = Arrays.asList(foundLink);
        Member author = Mockito.mock(Member.class);
        when(message.getMember()).thenReturn(author);
        when(userInServerManagementService.loadOrCreateUser(author)).thenReturn(userInAServer);
        when(messageEmbedService.getLinksInMessage(text)).thenReturn(foundMessageLinks);
        when(model.getMessage()).thenReturn(message);
        testUnit.execute(model);
        verify(messageService, times(0)).deleteMessage(message);
        verify(self, times(0)).embedSingleLink(eq(message), anyLong(), any(CachedMessage.class));
        verify(messageCache, times(0)).getMessageFromCache(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testTwoLinksOneGetsEmbedded() {
        String firstText = "link";
        String secondText = "secondLink";
        MessageEmbedLink differentOriginLink = Mockito.mock(MessageEmbedLink.class);
        when(differentOriginLink.getServerId()).thenReturn(SECOND_SERVER_ID);
        when(differentOriginLink.getMessageId()).thenReturn(SECOND_MESSAGE_ID);
        MessageEmbedLink sameServerLink = Mockito.mock(MessageEmbedLink.class);
        when(sameServerLink.getServerId()).thenReturn(FIRST_SERVER_ID);
        when(sameServerLink.getChannelId()).thenReturn(FIRST_CHANNEL_ID);
        when(sameServerLink.getMessageId()).thenReturn(FIRST_MESSAGE_ID);
        when(sameServerLink.getWholeUrl()).thenReturn(secondText);
        List<MessageEmbedLink> foundMessageLinks = Arrays.asList(differentOriginLink, sameServerLink);
        AUserInAServer embeddingUser = Mockito.mock(AUserInAServer.class);
        when(embeddingUser.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        String completeMessage = firstText.concat(secondText);
        when(message.getContentRaw()).thenReturn(completeMessage);
        setupMessageConfig();
        Member author = Mockito.mock(Member.class);
        when(message.getMember()).thenReturn(author);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(FIRST_SERVER_ID);
        when(userInServerManagementService.loadOrCreateUser(author)).thenReturn(embeddingUser);
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(messageCache.getMessageFromCache(FIRST_SERVER_ID, FIRST_CHANNEL_ID, FIRST_MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(cachedMessage));
        when(messageEmbedService.getLinksInMessage(completeMessage)).thenReturn(foundMessageLinks);
        when(model.getMessage()).thenReturn(message);
        testUnit.execute(model);
        verify(messageService, times(0)).deleteMessage(message);
        verify(self, times(1)).embedSingleLink(message, USER_IN_SERVER_ID, cachedMessage);
    }

    @Test
    public void testMultipleLinksFound() {
        String text = "link";
        MessageEmbedLink foundLink = Mockito.mock(MessageEmbedLink.class);
        when(foundLink.getServerId()).thenReturn(FIRST_SERVER_ID);
        when(foundLink.getChannelId()).thenReturn(FIRST_CHANNEL_ID);
        when(foundLink.getMessageId()).thenReturn(FIRST_MESSAGE_ID);
        when(foundLink.getWholeUrl()).thenReturn(text);
        MessageEmbedLink secondLink = Mockito.mock(MessageEmbedLink.class);
        when(secondLink.getServerId()).thenReturn(FIRST_SERVER_ID);
        when(secondLink.getChannelId()).thenReturn(FIRST_CHANNEL_ID);
        when(secondLink.getMessageId()).thenReturn(SECOND_MESSAGE_ID);
        when(secondLink.getWholeUrl()).thenReturn(text);
        List<MessageEmbedLink> foundMessageLinks = Arrays.asList(foundLink, secondLink);
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        when(userInAServer.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
        when(message.getContentRaw()).thenReturn(text);
        setupMessageConfig();
        Member author = Mockito.mock(Member.class);
        when(message.getMember()).thenReturn(author);
        when(userInServerManagementService.loadOrCreateUser(author)).thenReturn(userInAServer);
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        CachedMessage secondCachedMessage = Mockito.mock(CachedMessage.class);
        when(messageCache.getMessageFromCache(FIRST_SERVER_ID, FIRST_CHANNEL_ID, FIRST_MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(cachedMessage));
        when(messageCache.getMessageFromCache(FIRST_SERVER_ID, FIRST_CHANNEL_ID, SECOND_MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(secondCachedMessage));
        when(messageEmbedService.getLinksInMessage(text)).thenReturn(foundMessageLinks);
        when(model.getMessage()).thenReturn(message);
        testUnit.execute(model);
        verify(messageService, times(1)).deleteMessage(message);
        verify(self, times(1)).embedSingleLink(message, USER_IN_SERVER_ID, cachedMessage);
        verify(self, times(1)).embedSingleLink(message, USER_IN_SERVER_ID, secondCachedMessage);
    }

    @Test
    public void testLoadUserAndEmbed() {
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        long userId = 3L;
        when(message.getTextChannel()).thenReturn(textChannel);
        when(messageEmbedService.embedLink(cachedMessage, textChannel, userId, message)).thenReturn(CompletableFuture.completedFuture(null));
        testUnit.embedSingleLink(message, userId, cachedMessage);
        verify(metricService, times(1)).incrementCounter(any());
    }

    private void executeLinkEmbedTest(String linkText, String text) {
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        when(message.getContentRaw()).thenReturn(text);
        setupMessageConfig();
        MessageEmbedLink foundLink = Mockito.mock(MessageEmbedLink.class);
        when(foundLink.getWholeUrl()).thenReturn(linkText);
        when(foundLink.getMessageId()).thenReturn(FIRST_MESSAGE_ID);
        when(foundLink.getServerId()).thenReturn(FIRST_SERVER_ID);
        when(foundLink.getChannelId()).thenReturn(FIRST_CHANNEL_ID);
        List<MessageEmbedLink> foundMessageLinks = Arrays.asList(foundLink);
        Member author = Mockito.mock(Member.class);
        when(message.getMember()).thenReturn(author);
        when(userInServerManagementService.loadOrCreateUser(author)).thenReturn(userInAServer);
        CachedMessage cachedMessage = Mockito.mock(CachedMessage.class);
        when(messageCache.getMessageFromCache(FIRST_SERVER_ID, FIRST_CHANNEL_ID, FIRST_MESSAGE_ID)).thenReturn(CompletableFuture.completedFuture(cachedMessage));
        when(messageEmbedService.getLinksInMessage(text)).thenReturn(foundMessageLinks);
        when(model.getMessage()).thenReturn(message);
        testUnit.execute(model);
    }

}
