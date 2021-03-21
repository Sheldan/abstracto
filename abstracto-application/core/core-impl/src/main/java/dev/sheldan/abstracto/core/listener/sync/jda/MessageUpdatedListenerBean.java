package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageTextUpdatedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.ACTION;
import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.MESSAGE_METRIC;

@Component
@Slf4j
public class MessageUpdatedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<MessageTextUpdatedListener> listenerList;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageUpdatedListenerBean self;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private MetricService metricService;

    private static final CounterMetric MESSAGE_UPDATED_COUNTER = CounterMetric
            .builder()
            .name(MESSAGE_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(ACTION, "updated")))
            .build();

    @Override
    @Transactional
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        metricService.incrementCounter(MESSAGE_UPDATED_COUNTER);
        if(listenerList == null) return;
        Message message = event.getMessage();
        messageCache.getMessageFromCache(message.getGuild().getIdLong(), message.getTextChannel().getIdLong(), event.getMessageIdLong()).thenAccept(cachedMessage -> {
            self.executeListener(cachedMessage, event);
            messageCache.putMessageInCache(message);
        }).exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessage().getId(), throwable);
            return null;
        });

    }

    @Transactional
    public void executeListener(CachedMessage cachedMessage, GuildMessageUpdateEvent event) {
        MessageTextUpdatedModel model = getModel(event, cachedMessage);
        listenerList.forEach(messageTextUpdatedListener -> listenerService.executeFeatureAwareListener(messageTextUpdatedListener, model));
    }

    private MessageTextUpdatedModel getModel(GuildMessageUpdateEvent event, CachedMessage oldMessage) {
        return MessageTextUpdatedModel
                .builder()
                .after(event.getMessage())
                .before(oldMessage)
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_UPDATED_COUNTER, "Messages updated");
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
