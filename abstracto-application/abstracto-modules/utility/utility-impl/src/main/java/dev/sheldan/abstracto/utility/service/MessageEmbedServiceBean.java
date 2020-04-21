package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.template.listener.MessageEmbeddedModel;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.models.MessageEmbedLink;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MessageEmbedServiceBean implements MessageEmbedService {

    private Pattern messageRegex = Pattern.compile("(?<whole>https://discordapp.com/channels/(?<server>\\d+)/(?<channel>\\d+)/(?<message>\\d+)(?:.*?))+");

    public static final String MESSAGE_EMBED_TEMPLATE = "message";
    public static final String REMOVAL_EMOTE = "removeEmbed";

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageEmbedService self;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageEmbedPostManagementService messageEmbedPostManagementService;

    @Autowired
    private MessageService messageService;

    @Override
    public List<MessageEmbedLink> getLinksInMessage(String message) {
        List<MessageEmbedLink> links = new ArrayList<>();
        Matcher matcher = messageRegex.matcher(message);
        while(matcher.find()) {
            String serverId = matcher.group("server");
            String channelId = matcher.group("channel");
            String messageId = matcher.group("message");
            String wholeLink = matcher.group("whole");
            Long serverIdLong = Long.parseLong(serverId);
            Long channelIdLong = Long.parseLong(channelId);
            Long messageIdLong = Long.parseLong(messageId);
            MessageEmbedLink messageEmbedLink = MessageEmbedLink
                    .builder()
                    .serverId(serverIdLong)
                    .channelId(channelIdLong)
                    .messageId(messageIdLong)
                    .wholeUrl(wholeLink)
                    .build();
            links.add(messageEmbedLink);
        }
        return links;
    }

    @Override
    public void embedLinks(List<MessageEmbedLink> linksToEmbed, TextChannel target, AUserInAServer reason, Message embeddingMessage) {
        linksToEmbed.forEach(messageEmbedLink -> {
            messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId())
                    .thenAccept(cachedMessage -> {
                        self.embedLink(cachedMessage, target, reason, embeddingMessage);
                    });
        });
    }

    @Override
    @Transactional
    public void embedLink(CachedMessage cachedMessage, TextChannel target, AUserInAServer cause, Message embeddingMessage) {
        MessageEmbeddedModel messageEmbeddedModel = buildTemplateParameter(embeddingMessage, cachedMessage);
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel);
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToEndInTextChannel(embed, target);
        log.trace("Embedding message {} from channel {} from server {}, because of user {}", cachedMessage.getMessageId(),
                cachedMessage.getChannelId(), cachedMessage.getServerId(), cause.getUserReference().getId());
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            try {
                Message createdMessage = completableFutures.get(0).get();
                messageEmbedPostManagementService.createMessageEmbed(cachedMessage, createdMessage, cause);
                messageService.addReactionToMessage(REMOVAL_EMOTE, cachedMessage.getServerId(), createdMessage);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to post message embed.", e);
            }
        });

    }

    private MessageEmbeddedModel buildTemplateParameter(Message message, CachedMessage embeddedMessage) {
        AChannel channel = channelManagementService.loadChannel(message.getChannel().getIdLong());
        AServer server = serverManagementService.loadOrCreate(message.getGuild().getIdLong());
        AUserInAServer user = userManagementService.loadUser(message.getMember());
        Member author = botService.getMemberInServer(embeddedMessage.getServerId(), embeddedMessage.getAuthorId());
        TextChannel sourceChannel = botService.getTextChannelFromServer(embeddedMessage.getServerId(), embeddedMessage.getChannelId()).get();
        return MessageEmbeddedModel
                .builder()
                .channel(channel)
                .server(server)
                .member(message.getMember())
                .aUserInAServer(user)
                .author(author)
                .sourceChannel(sourceChannel)
                .embeddingUser(message.getMember())
                .user(user.getUserReference())
                .messageChannel(message.getChannel())
                .guild(message.getGuild())
                .embeddedMessage(embeddedMessage)
                .build();
    }
}
