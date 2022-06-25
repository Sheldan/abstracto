package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MessageReceivedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired(required = false)
    private List<MessageReceivedListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private MetricService metricService;

    public static final String MESSAGE_METRIC = "message";
    public static final String ACTION = "action";
    private static final CounterMetric MESSAGE_RECEIVED_COUNTER = CounterMetric
            .builder()
            .name(MESSAGE_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(ACTION, "received")))
            .build();

    @Override
    @Transactional
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        metricService.incrementCounter(MESSAGE_RECEIVED_COUNTER);
        messageCache.putMessageInCache(event.getMessage());
        if(listenerList == null) return;
        MessageReceivedModel model = getModel(event);
        listenerList.forEach(messageReceivedListener -> listenerService.executeFeatureAwareListener(messageReceivedListener, model));

    }

    private MessageReceivedModel getModel(MessageReceivedEvent event) {
        return MessageReceivedModel
                .builder()
                .message(event.getMessage())
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_RECEIVED_COUNTER, "Message received");
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
