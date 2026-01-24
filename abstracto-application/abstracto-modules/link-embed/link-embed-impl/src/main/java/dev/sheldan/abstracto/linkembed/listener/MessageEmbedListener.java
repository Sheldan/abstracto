package dev.sheldan.abstracto.linkembed.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.ConsumableListenerResult;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.GuildMemberMessageChannel;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureDefinition;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import dev.sheldan.abstracto.linkembed.service.MessageEmbedService;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MessageEmbedListener implements MessageReceivedListener {

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MessageEmbedService messageEmbedService;

    @Autowired
    private MessageEmbedListener self;

    @Autowired
    private MetricService metricService;

    @Autowired
    private MessageService messageService;

    public static final String MESSAGE_EMBEDDED = "message.embedded";
    public static final String MESSAGE_EMBED_ACTION = "action";
    private static final CounterMetric MESSAGE_EMBED_CREATED = CounterMetric
            .builder()
            .name(MESSAGE_EMBEDDED)
            .tagList(Arrays.asList(MetricTag.getTag(MESSAGE_EMBED_ACTION, "created")))
            .build();

    @Override
    public ConsumableListenerResult execute(MessageReceivedModel model) {
        Message message = model.getMessage();
        if(!message.isFromGuild() || message.isWebhookMessage() || message.getType().isSystem()) {
            return ConsumableListenerResult.IGNORED;
        }
        String messageRaw = message.getContentRaw();
        List<MessageEmbedLink> links = messageEmbedService.getLinksInMessage(messageRaw);
        for (MessageEmbedLink messageEmbedLink : links) {
            messageRaw = messageRaw.replace(messageEmbedLink.getWholeUrl(), "");
        }
        boolean deleteMessage = StringUtils.isBlank(messageRaw) && !links.isEmpty() && message.getAttachments().isEmpty();
        if(!links.isEmpty()) {
            Long messageId = message.getIdLong();
            Long channelId = message.getChannelIdLong();
            Long serverId = message.getGuildIdLong();
            log.debug("We found {} links to embed in message {} in channel {} in guild {}.", links.size(), message.getId(), message.getChannel().getId(), message.getGuild().getId());
            Long userEmbeddingUserInServerId = userInServerManagementService.loadOrCreateUser(message.getMember()).getUserInServerId();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (MessageEmbedLink messageEmbedLink : links) {
                // potentially support foreign linked servers?
                if(!messageEmbedLink.getServerId().equals(message.getGuild().getIdLong())) {
                    log.info("Link for message {} was from a foreign server {}. Do not embed.", messageEmbedLink.getMessageId(), messageEmbedLink.getServerId());
                    continue;
                }
                Function<CachedMessage, CompletableFuture<Void>> cachedMessageConsumer = cachedMessage -> self.embedSingleLink(message, userEmbeddingUserInServerId, cachedMessage);
                futures.add(messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId())
                        .thenCompose(cachedMessageConsumer));
            }
            if(!futures.isEmpty()) {
                // only delete the message if all futures go through
                new CompletableFutureList<>(futures).getMainFuture().thenAccept(unused -> {
                    if(deleteMessage) {
                        messageService.deleteMessageInChannelInServer(serverId, channelId, messageId);
                    }
                }).thenAccept(unused -> {
                    log.info("Deleted embedding message server {} channel {} message {}.", serverId, channelId, messageId);
                }).exceptionally(throwable -> {
                    log.info("Failed to delete embedding message or to embed message server {} channel {} message {}.", serverId, channelId, messageId, throwable);
                    return null;
                });
            }
        }
        if(deleteMessage) {
            return ConsumableListenerResult.DELETED;
        } else if(!links.isEmpty()) {
            return ConsumableListenerResult.PROCESSED;
        }
        return ConsumableListenerResult.IGNORED;
    }

    @Transactional
    public CompletableFuture<Void> embedSingleLink(Message message, Long cause, CachedMessage cachedMessage) {
        GuildMemberMessageChannel context = GuildMemberMessageChannel
                .builder()
                .guildChannel(message.getGuildChannel())
                .member(message.getMember())
                .guild(message.getGuild())
                .message(message)
                .build();
        log.info("Embedding link to message {} in channel {} in server {} to channel {} and server {}.",
                cachedMessage.getMessageId(), cachedMessage.getChannelId(), cachedMessage.getServerId(), message.getChannel().getId(), message.getGuild().getId());
        return messageEmbedService.embedLink(cachedMessage, message.getGuildChannel(), cause , context).thenAccept(unused ->
            metricService.incrementCounter(MESSAGE_EMBED_CREATED)
        ).exceptionally(throwable -> {
            log.error("Failed to embed link towards message {} in channel {} in sever {} linked from message {} in channel {} in server {}.", cachedMessage.getMessageId(), cachedMessage.getChannelId(), cachedMessage.getServerId(),
                    message.getId(), message.getChannel().getId(), message.getGuild().getId(), throwable);
            return null;
        });
    }

    @Override
    public boolean shouldConsume(Event event, ConsumableListenerResult result) {
        return result.equals(ConsumableListenerResult.DELETED);
    }

    @Override
    public FeatureDefinition getFeature() {
        return LinkEmbedFeatureDefinition.LINK_EMBEDS;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_EMBED_CREATED, "Message embeds created");
    }
}
