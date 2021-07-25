package dev.sheldan.abstracto.core.metric;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;

@Component
@Slf4j
public class JDAMetrics extends ListenerAdapter {
    @Autowired
    private MetricService metricService;

    public static final String JDA_EVENT_METRIC = "jda.event";
    public static final String EVENT_CLASS = "event.class";
    private final HashMap<Class, CounterMetric> coveredEvents = new HashMap<>();

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        if(!coveredEvents.containsKey(event.getClass())) {
            String eventName = event.getClass().getSimpleName();
            CounterMetric metric =
                    CounterMetric
                            .builder()
                            .tagList(Arrays.asList(MetricTag.getTag(EVENT_CLASS, eventName)))
                            .name(JDA_EVENT_METRIC)
                            .build();
            log.info("Registering new metric for event {}. There are now {} metrics.", eventName, coveredEvents.size());
            metricService.registerCounter(metric, "Events of type " + eventName);
            coveredEvents.put(event.getClass(), metric);
        }
        metricService.incrementCounter(coveredEvents.get(event.getClass()));
    }

}
