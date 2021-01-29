package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.metrics.service.CounterMetric;
import dev.sheldan.abstracto.core.metrics.service.MetricService;
import dev.sheldan.abstracto.experience.models.ServerExperience;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RunTimeExperienceService {

    @Autowired
    private MetricService metricService;

    public static final String EXPERIENCE_RUNTIME_STORAGE = "experience.runtime.storage";
    private static final CounterMetric EXPERIENCE_RUNTIME_STORAGE_METRIC = CounterMetric
            .builder()
            .name(EXPERIENCE_RUNTIME_STORAGE)
            .build();

    private Map<Long, List<ServerExperience>> runtimeExperience = new HashMap<>();
    private static final Lock lock = new ReentrantLock();
    public Map<Long, List<ServerExperience>> getRuntimeExperience() {
        return runtimeExperience;
    }

    public void takeLock() {
        lock.lock();
    }

    public void releaseLock() {
        lock.unlock();
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerGauge(EXPERIENCE_RUNTIME_STORAGE_METRIC, runtimeExperience, serverList -> serverList.values().stream()
                        .mapToInt(minuteEntry -> minuteEntry.stream()
                        .mapToInt(individualServerList -> individualServerList.getUserInServerIds().size()).sum()).sum(),
                "Number of entries in runtime experience storage");
    }

}