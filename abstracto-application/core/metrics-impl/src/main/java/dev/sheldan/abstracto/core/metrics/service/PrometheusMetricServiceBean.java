package dev.sheldan.abstracto.core.metrics.service;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

@Component
@Slf4j
public class PrometheusMetricServiceBean implements MetricService {

    private final List<Counter> counters = new ArrayList<>();
    private final List<Gauge> gauges = new ArrayList<>();

    @Autowired
    private MeterRegistry registry;

    @Override
    public void registerCounter(CounterMetric metric, String description) {
        if(doesCounterExist(metric)) {
            throw new IllegalArgumentException("Counter metric already exists.");
        }
        List<Tag> micrometerTags = new ArrayList<>();
        metric.getTagList().forEach(metricTag ->
                micrometerTags.add(Tag.of(metricTag.getKey(), metricTag.getValue()))
        );
        Counter counter = Counter.builder(metric.getName())
                .tags(micrometerTags)
                .description(description)
                .register(registry);
        counters.add(counter);
    }

    @Override
    public void registerGauge(CounterMetric counterMetric, Supplier<Number> f, String help) {
        registerGauge(counterMetric, f, help, null);
    }

    @Override
    public void registerGauge(CounterMetric counterMetric, Supplier<Number> f, String help, String baseUnit) {
        List<Tag> micrometerTags = new ArrayList<>();
        counterMetric.getTagList().forEach(metricTag ->
                micrometerTags.add(Tag.of(metricTag.getKey(), metricTag.getValue()))
        );

        Gauge gauge = Gauge
                .builder(counterMetric.getName(), f)
                .tags(micrometerTags)
                .baseUnit(baseUnit)
                .description(help)
                .register(registry);
        gauges.add(gauge);
    }

    @Override
    public <T> void registerGauge(CounterMetric counterMetric, T obj, ToDoubleFunction<T> f, String help) {
        registerGauge(counterMetric, obj, f, help, null);
    }

    @Override
    public <T> void registerGauge(CounterMetric counterMetric, T obj, ToDoubleFunction<T> f, String help, String baseUnit) {
        List<Tag> micrometerTags = new ArrayList<>();
        counterMetric.getTagList().forEach(metricTag ->
                micrometerTags.add(Tag.of(metricTag.getKey(), metricTag.getValue()))
        );

        Gauge gauge = Gauge
                .builder(counterMetric.getName(), obj, f)
                .tags(micrometerTags)
                .baseUnit(baseUnit)
                .description(help)
                .register(registry);
        gauges.add(gauge);
    }

    @Override
    public void incrementCounter(CounterMetric counterMetric) {
        incrementCounter(counterMetric, 1L);
    }

    @Override
    public void incrementCounter(CounterMetric counterMetric, Long amount) {
        Optional<Counter> counterOptional = counters.stream().filter(counter -> compareCounterIdAndCounterMetric(counter.getId(), counterMetric)).findFirst();
        counterOptional.ifPresent(counter -> counter.increment(amount));
        if(!counterOptional.isPresent()) {
            log.warn("Trying to increment counter {} with tags {}, which was not available (yet).", counterMetric.getName(), String.join(counterMetric.getTagList().toString()));
        }
    }

    private boolean doesCounterExist(CounterMetric counterMetric) {
        return counters.stream().anyMatch(counter -> compareCounterIdAndCounterMetric(counter.getId(), counterMetric));
    }

    private boolean compareCounterIdAndCounterMetric(Meter.Id id, CounterMetric counterMetric) {
        boolean nameEquals = id.getName().equals(counterMetric.getName());
        if(!nameEquals) {
            return false;
        }
        // in case there are global tags we don't know about them, so we only need to check if the one we are searching matches with the one we know
        return counterMetric.getTagList().stream().allMatch(metricTag ->
                id.getTags().stream().anyMatch(tag -> tag.getKey().equals(metricTag.getKey()) && tag.getValue().equals(metricTag.getValue()))
        );
    }

}