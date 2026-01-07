package dev.sheldan.abstracto.core.metric;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.http.HttpRequestEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.Route;
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
    public static final String JDA_REQUEST_METRIC = "jda.request";
    public static final String EVENT_CLASS = "event.class";
    public static final String HTTP_METHOD = "http.method";
    public static final String HTTP_URL = "http.url";
    public static final String HTTP_KEY = "http.key";
    private final HashMap<Class<?>, CounterMetric> coveredEvents = new HashMap<>();
    private final HashMap<String, CounterMetric> coveredRoutes = new HashMap<>();

    @Override
    public void onGenericEvent(GenericEvent event) {
        if(!coveredEvents.containsKey(event.getClass())) {
            String eventName = event.getClass().getSimpleName();
            CounterMetric metric =
                    CounterMetric
                            .builder()
                            .tagList(Arrays.asList(MetricTag.getTag(EVENT_CLASS, eventName)))
                            .name(JDA_EVENT_METRIC)
                            .build();
            metricService.registerCounter(metric, "Events of type " + eventName);
            coveredEvents.put(event.getClass(), metric);
            log.info("Registering new metric for event {}. There are now {} metrics.", eventName, coveredEvents.size());
        }
        metricService.incrementCounter(coveredEvents.get(event.getClass()));
    }

    @Override
    public void onHttpRequest(HttpRequestEvent event) {
        Route baseRoute = event.getRequest().getRoute().getBaseRoute();
        String urlKey = baseRoute.toString();
        if(!coveredRoutes.containsKey(urlKey)) {
            CounterMetric metric =
                CounterMetric
                    .builder()
                    .tagList(Arrays.asList(
                        MetricTag.getTag(HTTP_METHOD, baseRoute.getMethod().name()),
                        MetricTag.getTag(HTTP_URL, baseRoute.getRoute()),
                        MetricTag.getTag(HTTP_KEY, baseRoute.toString())))
                    .name(JDA_REQUEST_METRIC)
                    .build();
            metricService.registerCounter(metric, "Requests towards URL " + urlKey);
            coveredRoutes.put(urlKey, metric);
            log.info("Registering new metric for URL {}. There are now {} metrics.", urlKey, coveredRoutes.size());
        }
        metricService.incrementCounter(coveredRoutes.get(urlKey));
    }

}
