package dev.sheldan.abstracto.core.listener.combo;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageUpdatedListener;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageUpdatedListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.cache.CachedAttachment;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageUpdatedModel;
import dev.sheldan.abstracto.core.service.CacheEntityService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.ACTION;
import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.MESSAGE_METRIC;

@Component
@Slf4j
public class MessageUpdatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<MessageUpdatedListener> listenerList;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageUpdatedListenerBean self;

    @Autowired
    private ListenerService listenerService;

    @Autowired(required = false)
    private List<AsyncMessageUpdatedListener> asyncListenerList;

    @Autowired
    @Qualifier("messageUpdatedExecutor")
    private TaskExecutor messageUpdatedExecutor;

    @Autowired
    private MetricService metricService;

    private static final CounterMetric MESSAGE_UPDATED_COUNTER = CounterMetric
            .builder()
            .name(MESSAGE_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(ACTION, "updated")))
            .build();

    @Override
    @Transactional
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        metricService.incrementCounter(MESSAGE_UPDATED_COUNTER);
        Message message = event.getMessage();
        messageCache.getMessageFromCache(message.getGuild().getIdLong(), message.getChannel().getIdLong(), event.getMessageIdLong()).thenAccept(cachedMessage -> {
            try {
                // we need to provide a copy of the object, so modifications here dont influence the async execution
                // because we do modify it, as we are the one responsible for caching it
                executeAsyncListeners(event, SerializationUtils.clone(cachedMessage));
                self.executeListener(cachedMessage, event);
            } finally {
                cachedMessage.setContent(message.getContentRaw());
                List<CachedAttachment> remainingAttachments = cachedMessage.getAttachments().stream().filter(cachedAttachment ->
                        message.getAttachments().stream().anyMatch(attachment -> attachment.getIdLong() == cachedAttachment.getId())
                ).collect(Collectors.toList());
                cachedMessage.setAttachments(remainingAttachments);
                messageCache.putMessageInCache(cachedMessage);
            }
        }).exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessage().getId(), throwable);
            return null;
        });

    }

    @Transactional
    public void executeListener(CachedMessage cachedMessage, MessageUpdateEvent event) {
        if(listenerList == null) return;
        MessageUpdatedModel model = getModel(event, cachedMessage);
        listenerList.forEach(messageUpdatedListener -> listenerService.executeFeatureAwareListener(messageUpdatedListener, model));
    }

    private MessageUpdatedModel getModel(MessageUpdateEvent event, CachedMessage oldMessage) {
        return MessageUpdatedModel
                .builder()
                .after(event.getMessage())
                .before(oldMessage)
                .build();
    }

    private void executeAsyncListeners(MessageUpdateEvent event, CachedMessage oldMessage) {
        if(asyncListenerList == null) return;
        MessageUpdatedModel model = getModel(event, oldMessage);
        asyncListenerList.forEach(messageTextUpdatedListener ->
                listenerService.executeFeatureAwareListener(messageTextUpdatedListener, model, messageUpdatedExecutor)
        );
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_UPDATED_COUNTER, "Messages updated");
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
