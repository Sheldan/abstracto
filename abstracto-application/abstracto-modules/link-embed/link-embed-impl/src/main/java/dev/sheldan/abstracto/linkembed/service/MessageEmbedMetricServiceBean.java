package dev.sheldan.abstracto.linkembed.service;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

import static dev.sheldan.abstracto.linkembed.listener.MessageEmbedListener.MESSAGE_EMBEDDED;
import static dev.sheldan.abstracto.linkembed.listener.MessageEmbedListener.MESSAGE_EMBED_ACTION;

@Component
public class MessageEmbedMetricServiceBean implements MessageEmbedMetricService {

    private static final CounterMetric MESSAGE_EMBED_REMOVED_CREATOR = CounterMetric
            .builder()
            .name(MESSAGE_EMBEDDED)
            .tagList(Arrays.asList(MetricTag.getTag(MESSAGE_EMBED_ACTION, "removed.creator")))
            .build();

    private static final CounterMetric MESSAGE_EMBED_REMOVED_SOURCE = CounterMetric
            .builder()
            .name(MESSAGE_EMBEDDED)
            .tagList(Arrays.asList(MetricTag.getTag(MESSAGE_EMBED_ACTION, "removed.source")))
            .build();

    @Autowired
    private MetricService metricService;

    @Override
    public void incrementMessageEmbedDeletedMetric(boolean embeddedUserDeleted) {
        if(embeddedUserDeleted) {
            incrementMessageEmbedDeletedEmbeddedMetric();
        } else {
            incrementMessageEmbedDeletedEmbeddingMetric();
        }
    }

    @Override
    public void incrementMessageEmbedDeletedEmbeddedMetric() {
        metricService.incrementCounter(MESSAGE_EMBED_REMOVED_SOURCE);
    }

    @Override
    public void incrementMessageEmbedDeletedEmbeddingMetric() {
        metricService.incrementCounter(MESSAGE_EMBED_REMOVED_CREATOR);
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_EMBED_REMOVED_CREATOR, "Message embeds which are deleted by the embedding user.");
        metricService.registerCounter(MESSAGE_EMBED_REMOVED_SOURCE, "Message embeds which are deleted by the embedded user.");
    }

}
