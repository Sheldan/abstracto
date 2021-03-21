package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageDeletedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.ACTION;
import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.MESSAGE_METRIC;

@Component
@Slf4j
public class MessageDeletedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<MessageDeletedListener> listenerList;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MetricService metricService;

    @Autowired
    private ListenerService listenerService;

    private static final CounterMetric MESSAGE_DELETED_COUNTER =
            CounterMetric
                    .builder().name(MESSAGE_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(ACTION, "deleted")))
                    .build();

    @Override
    @Transactional
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        metricService.incrementCounter(MESSAGE_DELETED_COUNTER);
        if(listenerList == null) return;
        Consumer<CachedMessage> cachedMessageConsumer =  cachedMessage -> {
            MessageDeletedModel model = getModel(cachedMessage);
            listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model));
        };
        messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong())
                .thenAccept(cachedMessageConsumer)
                .exceptionally(throwable -> {
                    log.error("Message retrieval {} from cache failed. ", event.getMessageIdLong(), throwable);
                    return null;
                });
    }

    private MessageDeletedModel getModel(CachedMessage cachedMessage) {
        return MessageDeletedModel.builder().cachedMessage(cachedMessage).build();
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_DELETED_COUNTER, "Messages deleted");
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
