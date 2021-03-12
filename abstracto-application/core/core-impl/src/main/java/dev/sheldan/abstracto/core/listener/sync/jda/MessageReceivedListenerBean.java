package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.execution.result.MessageReceivedListenerResult;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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
    private MessageReceivedListenerBean self;

    @Autowired
    private MetricService metricService;

    public static final String MESSAGE_METRIC = "message";
    public static final String ACTION = "action";
    private static final CounterMetric MESSAGE_RECEIVED_COUNTER = CounterMetric.builder().name(MESSAGE_METRIC).tagList(Arrays.asList(MetricTag.getTag(ACTION, "received"))).build();

    @Override
    @Transactional
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        metricService.incrementCounter(MESSAGE_RECEIVED_COUNTER);
        messageCache.putMessageInCache(event.getMessage());
        if(listenerList == null) return;
        for (MessageReceivedListener messageReceivedListener : listenerList) {
            try {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageReceivedListener.getFeature());
                if (!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                    continue;
                }
                MessageReceivedListenerResult result = self.executeIndividualGuildMessageReceivedListener(event, messageReceivedListener);
                if (messageReceivedListener.shouldConsume(event, result)) {
                    break;
                }
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
                exceptionService.reportExceptionToGuildMessageReceivedContext(e, event);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public MessageReceivedListenerResult executeIndividualGuildMessageReceivedListener(@Nonnull GuildMessageReceivedEvent event, MessageReceivedListener messageReceivedListener) {
        return messageReceivedListener.execute(event.getMessage());
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_RECEIVED_COUNTER, "Message received");
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
