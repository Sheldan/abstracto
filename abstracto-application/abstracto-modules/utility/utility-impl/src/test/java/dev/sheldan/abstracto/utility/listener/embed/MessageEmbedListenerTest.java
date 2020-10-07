package dev.sheldan.abstracto.utility.listener.embed;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.models.MessageEmbedLink;
import dev.sheldan.abstracto.utility.service.MessageEmbedService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.internal.requests.restaction.AuditableRestActionImpl;
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

    public static final long ORIGIN_GUILD_ID = 12L;
    public static final long EMBEDDING_GUILD_ID = 13L;
    @InjectMocks
    private MessageEmbedListener testUnit;

    @Mock
    private MessageCache messageCache;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private MessageEmbedService messageEmbedService;

    @Mock
    private MessageEmbedListener self;

    @Mock
    private Message message;

    @Mock
    private AuditableRestActionImpl<Void> deletionRestAction;

    @Mock
    private TextChannel textChannel;

    @Mock
    private Guild guild;

    @Before
    public void setup(){
        when(guild.getIdLong()).thenReturn(ORIGIN_GUILD_ID);
        when(message.getGuild()).thenReturn(guild);
        when(message.getChannel()).thenReturn(textChannel);
    }

    @Test
    public void testNoLinkFoundExecution() {
        String text = "text";
        when(message.getContentRaw()).thenReturn(text);
        List<MessageEmbedLink> foundMessageLinks = new ArrayList<>();
        when(messageEmbedService.getLinksInMessage(text)).thenReturn(foundMessageLinks);
        testUnit.execute(message);
        verify(message, times(0)).delete();
    }

    @Test
    public void testOnlyOneLinkFoundExecution() {
        String linkText = "link";
        String text = linkText;
        executeLinkTestForOneLink(text, linkText, ORIGIN_GUILD_ID, ORIGIN_GUILD_ID);
        verify(self, times(1)).loadUserAndEmbed(eq(message), anyLong(), any(CachedMessage.class));
        verify(message, times(1)).delete();
        verify(deletionRestAction, times(1)).queue();
    }

    @Test
    public void testOneLinkWithAdditionalTextExecution() {
        String linkText = "link";
        String text = linkText + "more text";
        executeLinkTestForOneLink(text, linkText, ORIGIN_GUILD_ID, ORIGIN_GUILD_ID);
        verify(self, times(1)).loadUserAndEmbed(eq(message), anyLong(), any(CachedMessage.class));
        verify(message, times(0)).delete();
        verify(deletionRestAction, times(0)).queue();
    }

    @Test
    public void testLinkFromDifferentServer() {
        String linkText = "link";
        String text = linkText + "more text";
        executeLinkTestForOneLink(text, linkText, ORIGIN_GUILD_ID, EMBEDDING_GUILD_ID);
        verify(message, times(0)).delete();
        verify(deletionRestAction, times(0)).queue();
        verify(self, times(0)).loadUserAndEmbed(eq(message), anyLong(), any(CachedMessage.class));
        verify(messageCache, times(0)).getMessageFromCache(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testTwoLinksOneGetsEmbedded() {
        String firstText = "link";
        String secondText = "secondLink";
        AServer originServer = MockUtils.getServer(ORIGIN_GUILD_ID);
        AServer embeddingServer = MockUtils.getServer(EMBEDDING_GUILD_ID);
        AChannel originChannel = MockUtils.getTextChannel(originServer, 4L);
        AChannel embeddingChannel = MockUtils.getTextChannel(embeddingServer, 7L);
        Long messageId = 4L;
        MessageEmbedLink differentOriginLink = MessageEmbedLink
                .builder()
                .serverId(originServer.getId())
                .channelId(originChannel.getId())
                .messageId(messageId)
                .wholeUrl(firstText)
                .build();
        Long secondMessageId = 5L;
        MessageEmbedLink sameServerLink = MessageEmbedLink
                .builder()
                .serverId(embeddingServer.getId())
                .channelId(embeddingChannel.getId())
                .messageId(secondMessageId)
                .wholeUrl(secondText)
                .build();
        List<MessageEmbedLink> foundMessageLinks = Arrays.asList(differentOriginLink, sameServerLink);
        AUserInAServer embeddingUser = MockUtils.getUserObject(4L, embeddingServer);
        String completeMessage = firstText.concat(secondText);
        when(message.getContentRaw()).thenReturn(completeMessage);

        Member author = Mockito.mock(Member.class);
        when(message.getMember()).thenReturn(author);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(EMBEDDING_GUILD_ID);
        when(userInServerManagementService.loadUser(author)).thenReturn(embeddingUser);
        CachedMessage cachedMessage = CachedMessage.builder().build();
        when(messageCache.getMessageFromCache(embeddingServer.getId(), embeddingChannel.getId(), secondMessageId)).thenReturn(CompletableFuture.completedFuture(cachedMessage));
        when(messageEmbedService.getLinksInMessage(completeMessage)).thenReturn(foundMessageLinks);
        testUnit.execute(message);
        verify(message, times(0)).delete();
        verify(deletionRestAction, times(0)).queue();
        verify(self, times(1)).loadUserAndEmbed(message, embeddingUser.getUserInServerId(), cachedMessage);
    }

    @Test
    public void testMultipleLinksFound() {
        String text = "link";
        AServer server = MockUtils.getServer(ORIGIN_GUILD_ID);
        AChannel channel = MockUtils.getTextChannel(server, 4L);
        Long messageId = 4L;
        MessageEmbedLink foundLink = MessageEmbedLink
                .builder()
                .serverId(server.getId())
                .channelId(channel.getId())
                .messageId(messageId)
                .wholeUrl(text)
                .build();
        Long secondMessageId = 5L;
        MessageEmbedLink secondLink = MessageEmbedLink
                .builder()
                .serverId(server.getId())
                .channelId(channel.getId())
                .messageId(secondMessageId)
                .wholeUrl(text)
                .build();
        List<MessageEmbedLink> foundMessageLinks = Arrays.asList(foundLink, secondLink);
        AUserInAServer userInAServer = MockUtils.getUserObject(4L, server);
        when(message.getContentRaw()).thenReturn(text);

        Member author = Mockito.mock(Member.class);
        when(message.getMember()).thenReturn(author);
        when(message.delete()).thenReturn(deletionRestAction);
        when(userInServerManagementService.loadUser(author)).thenReturn(userInAServer);
        CachedMessage cachedMessage = CachedMessage.builder().build();
        CachedMessage secondCachedMessage = CachedMessage.builder().build();
        when(messageCache.getMessageFromCache(server.getId(), channel.getId(), messageId)).thenReturn(CompletableFuture.completedFuture(cachedMessage));
        when(messageCache.getMessageFromCache(server.getId(), channel.getId(), secondMessageId)).thenReturn(CompletableFuture.completedFuture(secondCachedMessage));
        when(messageEmbedService.getLinksInMessage(text)).thenReturn(foundMessageLinks);
        testUnit.execute(message);
        verify(message, times(1)).delete();
        verify(deletionRestAction, times(1)).queue();
        verify(self, times(1)).loadUserAndEmbed(message, userInAServer.getUserInServerId(), cachedMessage);
        verify(self, times(1)).loadUserAndEmbed(message, userInAServer.getUserInServerId(), secondCachedMessage);
    }

    @Test
    public void testLoadUserAndEmbed() {
        CachedMessage cachedMessage = CachedMessage.builder().build();
        long userId = 3L;
        when(message.getTextChannel()).thenReturn(textChannel);
        testUnit.loadUserAndEmbed(message, userId, cachedMessage);
        verify(messageEmbedService, times(1)).embedLink(cachedMessage, textChannel, userId, message);
    }

    private void executeLinkTestForOneLink(String text, String linkText, Long originServerId, Long embeddingServerId) {
        AServer originServer = MockUtils.getServer(originServerId);
        AServer embeddingServer = MockUtils.getServer(embeddingServerId);
        AChannel channel = MockUtils.getTextChannel(embeddingServer, 4L);
        AUserInAServer userInAServer = MockUtils.getUserObject(4L, embeddingServer);
        when(message.getContentRaw()).thenReturn(text);
        Long messageId = 4L;
        MessageEmbedLink foundLink = MessageEmbedLink
                .builder()
                .serverId(embeddingServer.getId())
                .channelId(channel.getId())
                .messageId(messageId)
                .wholeUrl(linkText)
                .build();
        List<MessageEmbedLink> foundMessageLinks = Arrays.asList(foundLink);
        Member author = Mockito.mock(Member.class);
        when(message.getMember()).thenReturn(author);
        when(message.delete()).thenReturn(deletionRestAction);
        when(userInServerManagementService.loadUser(author)).thenReturn(userInAServer);
        CachedMessage cachedMessage = CachedMessage.builder().build();
        when(messageCache.getMessageFromCache(originServer.getId(), channel.getId(), messageId)).thenReturn(CompletableFuture.completedFuture(cachedMessage));
        when(messageEmbedService.getLinksInMessage(text)).thenReturn(foundMessageLinks);
        testUnit.execute(message);
    }

}
