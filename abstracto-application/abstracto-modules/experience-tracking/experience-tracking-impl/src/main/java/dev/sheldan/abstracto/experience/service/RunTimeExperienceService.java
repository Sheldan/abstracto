package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.experience.model.ServerExperience;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Responsible for maintaining the data structure containing the IDs of {@link dev.sheldan.abstracto.core.models.database.AUserInAServer}
 * for which experience should be given, because they sent messages on their respective servers
 */
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

    /**
     * Acquires the lock of the runtime experience data structure. Operations on it should only be done, while holding the lock
     */
    public void takeLock() {
        lock.lock();
    }

    /**
     * Releases the lock again, and allows others to take the lock, if they want to modify the data structure.
     */
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