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
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MessageEmbedServiceBean implements MessageEmbedService {

    private Pattern messageRegex = Pattern.compile("(?<whole>https://discord(?:app)?.com/channels/(?<server>\\d+)/(?<channel>\\d+)/(?<message>\\d+)(?:.*?))+");

    public static final String MESSAGE_EMBED_TEMPLATE = "message_embed";
    public static final String REMOVAL_EMOTE = "removeEmbed";

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageEmbedServiceBean self;

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
    public void embedLinks(List<MessageEmbedLink> linksToEmbed, TextChannel target, Long userEmbeddingUserInServerId, Message embeddingMessage) {
        linksToEmbed.forEach(messageEmbedLink ->
            messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId())
                    .thenAccept(cachedMessage -> self.embedLink(cachedMessage, target, userEmbeddingUserInServerId, embeddingMessage)

                    ).exceptionally(throwable -> {
                log.error("Message embedding from cache failed for message {}.", messageEmbedLink.getMessageId(), throwable);
                return null;
            })
        );
    }

    @Override
    @Transactional
    public CompletableFuture<Void> embedLink(CachedMessage cachedMessage, TextChannel target, Long userEmbeddingUserInServerId, Message embeddingMessage) {
        Optional<AUserInAServer> causeOpt = userInServerManagementService.loadUserOptional(userEmbeddingUserInServerId);
        if(causeOpt.isPresent()) {
            return buildTemplateParameter(embeddingMessage, cachedMessage).thenCompose(messageEmbeddedModel ->
                self.sendEmbeddingMessage(cachedMessage, target, userEmbeddingUserInServerId, messageEmbeddedModel)
            );
        } else {
            log.warn("User {} which was not found in database wanted to embed link in message {}.", userEmbeddingUserInServerId, embeddingMessage.getJumpUrl());
            return CompletableFuture.completedFuture(null);
        }
    }

    @Transactional
    public CompletionStage<Void> sendEmbeddingMessage(CachedMessage cachedMessage, TextChannel target, Long userEmbeddingUserInServerId, MessageEmbeddedModel messageEmbeddedModel) {
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel);
        AUserInAServer cause = userInServerManagementService.loadUser(userEmbeddingUserInServerId);
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(embed, target);
        log.trace("Embedding message {} from channel {} from server {}, because of user {}", cachedMessage.getMessageId(),
                cachedMessage.getChannelId(), cachedMessage.getServerId(), cause.getUserReference().getId());
        Long userInServerId = cause.getUserInServerId();
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenCompose(aVoid -> {
            Message createdMessage = completableFutures.get(0).join();
            return messageService.addReactionToMessageWithFuture(REMOVAL_EMOTE, cachedMessage.getServerId(), createdMessage).thenAccept(aVoid1 ->
                self.loadUserAndPersistMessage(cachedMessage, userInServerId, createdMessage)
            );
        });
    }

    @Transactional
    public void loadUserAndPersistMessage(CachedMessage cachedMessage, Long userInServerId, Message createdMessage) {
        AUserInAServer innerCause = userInServerManagementService.loadUser(userInServerId);
        messageEmbedPostManagementService.createMessageEmbed(cachedMessage, createdMessage, innerCause);
    }

    private CompletableFuture<MessageEmbeddedModel> buildTemplateParameter(Message message, CachedMessage embeddedMessage) {
        return botService.getMemberInServerAsync(embeddedMessage.getServerId(), embeddedMessage.getAuthor().getAuthorId()).thenApply(member ->
            self.loadMessageEmbedModel(message, embeddedMessage, member)
        );
    }

    @Transactional
    public MessageEmbeddedModel loadMessageEmbedModel(Message message, CachedMessage embeddedMessage, Member member) {
        AServer server = serverManagementService.loadOrCreate(message.getGuild().getIdLong());
        AUserInAServer user = userInServerManagementService.loadUser(message.getMember());
        Optional<TextChannel> textChannelFromServer = botService.getTextChannelFromServerOptional(embeddedMessage.getServerId(), embeddedMessage.getChannelId());
        TextChannel sourceChannel = textChannelFromServer.orElse(null);
        AChannel channel = channelManagementService.loadChannel(message.getChannel().getIdLong());
        return MessageEmbeddedModel
                .builder()
                .channel(channel)
                .server(server)
                .member(message.getMember())
                .aUserInAServer(user)
                .author(member)
                .sourceChannel(sourceChannel)
                .embeddingUser(message.getMember())
                .user(user.getUserReference())
                .messageChannel(message.getChannel())
                .guild(message.getGuild())
                .embeddedMessage(embeddedMessage)
                .build();
    }
}
