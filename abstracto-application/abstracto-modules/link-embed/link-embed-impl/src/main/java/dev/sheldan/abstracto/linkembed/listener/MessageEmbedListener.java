package dev.sheldan.abstracto.linkembed.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.execution.result.ExecutionResult;
import dev.sheldan.abstracto.core.execution.result.MessageReceivedListenerResult;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureDefinition;
import dev.sheldan.abstracto.linkembed.model.MessageEmbedLink;
import dev.sheldan.abstracto.linkembed.service.MessageEmbedService;
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
import java.util.function.Consumer;

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
    public MessageReceivedListenerResult execute(Message message) {
        String messageRaw = message.getContentRaw();
        List<MessageEmbedLink> links = messageEmbedService.getLinksInMessage(messageRaw);
        if(!links.isEmpty()) {
            log.trace("We found {} links to embed in message {} in channel {} in guild {}.", links.size(), message.getId(), message.getChannel().getId(), message.getGuild().getId());
            Long userEmbeddingUserInServerId = userInServerManagementService.loadOrCreateUser(message.getMember()).getUserInServerId();
            for (MessageEmbedLink messageEmbedLink : links) {
                if(!messageEmbedLink.getServerId().equals(message.getGuild().getIdLong())) {
                    log.info("Link for message {} was from a foreign server {}. Do not embed.", messageEmbedLink.getMessageId(), messageEmbedLink.getServerId());
                    continue;
                }
                messageRaw = messageRaw.replace(messageEmbedLink.getWholeUrl(), "");
                Consumer<CachedMessage> cachedMessageConsumer = cachedMessage -> self.embedSingleLink(message, userEmbeddingUserInServerId, cachedMessage);
                messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId())
                        .thenAccept(cachedMessageConsumer)
                        .exceptionally(throwable -> {
                            log.error("Error when embedding link for message {}", message.getId(), throwable);
                            return null;
                        });
            }
        }
        if(StringUtils.isBlank(messageRaw) && !links.isEmpty()) {
            messageService.deleteMessage(message);
            return MessageReceivedListenerResult.DELETED;
        }
        if(!links.isEmpty()) {
            return MessageReceivedListenerResult.PROCESSED;
        }
        return MessageReceivedListenerResult.IGNORED;
    }

    @Transactional
    public void embedSingleLink(Message message, Long cause, CachedMessage cachedMessage) {
        log.info("Embedding link to message {} in channel {} in server {} to channel {} and server {}.",
                cachedMessage.getMessageId(), cachedMessage.getChannelId(), cachedMessage.getServerId(), message.getChannel().getId(), message.getGuild().getId());
        messageEmbedService.embedLink(cachedMessage, message.getTextChannel(), cause , message).thenAccept(unused ->
            metricService.incrementCounter(MESSAGE_EMBED_CREATED)
        ).exceptionally(throwable -> {
            log.error("Failed to embed link towards message {} in channel {} in sever {} linked from message {} in channel {} in server {}.", cachedMessage.getMessageId(), cachedMessage.getChannelId(), cachedMessage.getServerId(),
                    message.getId(), message.getChannel().getId(), message.getGuild().getId(), throwable);
            return null;
        });
    }

    @Override
    public boolean shouldConsume(Event event, ExecutionResult result) {
        return result.equals(MessageReceivedListenerResult.DELETED);
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
