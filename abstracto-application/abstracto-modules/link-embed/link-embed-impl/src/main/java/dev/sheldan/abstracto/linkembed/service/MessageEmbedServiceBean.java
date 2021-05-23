package dev.sheldan.abstracto.linkembed.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.listener.MessageEmbeddedModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import dev.sheldan.abstracto.linkembed.service.management.MessageEmbedPostManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageEmbedServiceBean implements MessageEmbedService {

    private final Pattern messageRegex = Pattern.compile("(?<whole>(?:https?://)?(?:\\w+\\.)?discord(?:app)?\\.com/channels/(?<server>\\d+)/(?<channel>\\d+)/(?<message>\\d+)(?:.*?))+", Pattern.CASE_INSENSITIVE);

    public static final String MESSAGE_EMBED_TEMPLATE = "message_embed";
    public static final String REMOVAL_EMOTE = "removeEmbed";

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

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
    private ReactionService reactionService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmoteService emoteService;

    @Value("${abstracto.feature.linkEmbed.removalDays}")
    private Long embedRemovalDays;

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
        return buildTemplateParameter(embeddingMessage, cachedMessage).thenCompose(messageEmbeddedModel ->
            self.sendEmbeddingMessage(cachedMessage, target, userEmbeddingUserInServerId, messageEmbeddedModel)
        );
    }

    @Override
    public CompletableFuture<Void> cleanUpOldMessageEmbeds() {
        Instant oldestDate = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(embedRemovalDays, ChronoUnit.DAYS);
        List<EmbeddedMessage> embeddedMessages = messageEmbedPostManagementService.getEmbeddedMessagesOlderThan(oldestDate);
        if(embeddedMessages.isEmpty()) {
            log.info("No embedded messages to clean up.");
            return CompletableFuture.completedFuture(null);
        }
        log.info("Cleaning up {} embedded embeddedMessages", embeddedMessages.size());
        List<ServerChannelMessage> serverChannelMessages = embeddedMessages.stream().map(embeddedMessage ->
            ServerChannelMessage
                    .builder()
                    .serverId(embeddedMessage.getEmbeddingServer().getId())
                    .channelId(embeddedMessage.getEmbeddingChannel().getId())
                    .messageId(embeddedMessage.getEmbeddingMessageId())
                    .build()
        )
        .collect(Collectors.toList());
        List<Long> embeddedMessagesHandled = embeddedMessages
                .stream()
                .map(EmbeddedMessage::getEmbeddingMessageId)
                .collect(Collectors.toList());
        List<CompletableFuture<Message>> messageFutures = messageService.retrieveMessages(serverChannelMessages);
        CompletableFutureList<Message> future = new CompletableFutureList<>(messageFutures);
        return future.getMainFuture()
                .handle((unused, throwable) -> self.removeReactions(future.getObjects()))
                .thenCompose(Function.identity())
                // deleting the messages from db regardless of exceptions, at most the reaction remains
                .whenComplete((unused, throwable) -> self.deleteEmbeddedMessages(embeddedMessagesHandled))
                .exceptionally(throwable -> {
                    log.error("Failed to clean up embedded messages.", throwable);
                    return null;
                });
    }

    @Transactional
    public CompletableFuture<Void> removeReactions(List<Message> allMessages) {
        List<CompletableFuture<Void>> removalFutures = new ArrayList<>();
        Map<Long, List<Message>> groupedPerServer = allMessages
                .stream()
                .collect(Collectors.groupingBy(message -> message.getGuild().getIdLong()));
        groupedPerServer.forEach((serverId, serverMessages) -> {
            // we assume the emote remained the same
            CompletableFutureList<Void> removalFuture = reactionService.removeReactionFromMessagesWithFutureWithFutureList(serverMessages, REMOVAL_EMOTE);
            removalFutures.add(removalFuture.getMainFuture());
        });
        return FutureUtils.toSingleFutureGeneric(removalFutures);
    }

    @Transactional
    public void deleteEmbeddedMessages(List<Long> embeddedMessagesToDelete) {
        messageEmbedPostManagementService.deleteEmbeddedMessagesViaId(embeddedMessagesToDelete);
    }

    @Transactional
    public CompletableFuture<Void> sendEmbeddingMessage(CachedMessage cachedMessage, TextChannel target, Long userEmbeddingUserInServerId, MessageEmbeddedModel messageEmbeddedModel) {
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel, target.getGuild().getIdLong());
        AUserInAServer cause = userInServerManagementService.loadOrCreateUser(userEmbeddingUserInServerId);
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToSendToChannel(embed, target);
        log.debug("Embedding message {} from channel {} from server {}, because of user {}", cachedMessage.getMessageId(),
                cachedMessage.getChannelId(), cachedMessage.getServerId(), cause.getUserReference().getId());
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenCompose(aVoid -> {
            Message createdMessage = completableFutures.get(0).join();
            return reactionService.addReactionToMessageAsync(REMOVAL_EMOTE, cachedMessage.getServerId(), createdMessage).thenAccept(aVoid1 ->
                self.loadUserAndPersistMessage(cachedMessage, userEmbeddingUserInServerId, createdMessage)
            );
        });
    }

    @Transactional
    public void loadUserAndPersistMessage(CachedMessage cachedMessage, Long embeddingUserId, Message createdMessage) {
        AUserInAServer innerCause = userInServerManagementService.loadOrCreateUser(embeddingUserId);
        messageEmbedPostManagementService.createMessageEmbed(cachedMessage, createdMessage, innerCause);
    }

    private CompletableFuture<MessageEmbeddedModel> buildTemplateParameter(Message message, CachedMessage embeddedMessage) {
        return userService.retrieveUserForId(embeddedMessage.getAuthor().getAuthorId()).thenApply(authorUser ->
            self.loadMessageEmbedModel(message, embeddedMessage, authorUser)
        ).exceptionally(throwable -> {
            log.warn("Failed to retrieve author for user {}.", embeddedMessage.getAuthor().getAuthorId(), throwable);
            return self.loadMessageEmbedModel(message, embeddedMessage, null);
        });
    }

    @Transactional
    public MessageEmbeddedModel loadMessageEmbedModel(Message message, CachedMessage embeddedMessage, User userAuthor) {
        Optional<TextChannel> textChannelFromServer = channelService.getTextChannelFromServerOptional(embeddedMessage.getServerId(), embeddedMessage.getChannelId());
        TextChannel sourceChannel = textChannelFromServer.orElse(null);
        return MessageEmbeddedModel
                .builder()
                .member(message.getMember())
                .author(userAuthor)
                .sourceChannel(sourceChannel)
                .embeddingUser(message.getMember())
                .messageChannel(message.getChannel())
                .guild(message.getGuild())
                .embeddedMessage(embeddedMessage)
                .build();
    }
}
