package dev.sheldan.abstracto.core.metric;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@Component
@Slf4j
public class OkHttpMetrics implements Interceptor {

    @Autowired
    private MetricService metricService;

    public static final String MODERATION_PURGE_METRIC = "okhttp.request";
    public static final String HTTP_CODE = "http.code";

    private static final CounterMetric OKTTHP_200_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "200")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_201_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "201")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_204_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "204")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_202_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "202")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_401_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "401")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_403_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "403")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_404_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "404")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_429_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "429")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric OKTTHP_500_RESPONSE =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(HTTP_CODE, "500")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final HashMap<Integer, CounterMetric> METRICS = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        incrementCounter(response.code());
        return response;
    }

    private void incrementCounter(Integer code) {
        if(METRICS.containsKey(code)) {
            metricService.incrementCounter(METRICS.get(code));
        } else {
            log.warn("Unmapped response code from server {}.", code);
        }
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(OKTTHP_200_RESPONSE, "Amount of HTTP 200 responses in okhttp");
        metricService.registerCounter(OKTTHP_201_RESPONSE, "Amount of HTTP 201 responses in okhttp");
        metricService.registerCounter(OKTTHP_202_RESPONSE, "Amount of HTTP 202 responses in okhttp");
        metricService.registerCounter(OKTTHP_204_RESPONSE, "Amount of HTTP 204 responses in okhttp");
        metricService.registerCounter(OKTTHP_401_RESPONSE, "Amount of HTTP 401 responses in okhttp");
        metricService.registerCounter(OKTTHP_403_RESPONSE, "Amount of HTTP 403 responses in okhttp");
        metricService.registerCounter(OKTTHP_404_RESPONSE, "Amount of HTTP 404 responses in okhttp");
        metricService.registerCounter(OKTTHP_429_RESPONSE, "Amount of HTTP 429 responses in okhttp");
        metricService.registerCounter(OKTTHP_500_RESPONSE, "Amount of HTTP 500 responses in okhttp");
        METRICS.put(200, OKTTHP_200_RESPONSE);
        METRICS.put(201, OKTTHP_201_RESPONSE);
        METRICS.put(202, OKTTHP_202_RESPONSE);
        METRICS.put(204, OKTTHP_204_RESPONSE);
        METRICS.put(401, OKTTHP_401_RESPONSE);
        METRICS.put(403, OKTTHP_403_RESPONSE);
        METRICS.put(404, OKTTHP_404_RESPONSE);
        METRICS.put(429, OKTTHP_429_RESPONSE);
        METRICS.put(500, OKTTHP_500_RESPONSE);
    }
}
