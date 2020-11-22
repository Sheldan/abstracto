package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.experience.models.ServerExperience;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RunTimeExperienceService {
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

}
