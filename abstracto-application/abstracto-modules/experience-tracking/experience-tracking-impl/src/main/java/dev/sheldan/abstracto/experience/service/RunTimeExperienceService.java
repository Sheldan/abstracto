package dev.sheldan.abstracto.experience.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
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

    @Getter
    private final Map<Long,Map<Long, Instant>> runtimeExperience = new HashMap<>();
    private static final Lock lock = new ReentrantLock();

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

    public void cleanupRunTimeStorage() {
        Instant now = Instant.now();
        runtimeExperience.forEach((serverId, userInstantMap) -> {
            List<Long> userIdsToRemove = new ArrayList<>();
            userInstantMap.forEach((userId, instant) -> {
                if(instant.isBefore(now)) {
                    userIdsToRemove.add(userId);
                }
            });
            userIdsToRemove.forEach(userInstantMap::remove);
        });
    }
}