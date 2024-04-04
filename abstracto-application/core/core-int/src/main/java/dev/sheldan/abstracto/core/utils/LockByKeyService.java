package dev.sheldan.abstracto.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


@Component
@Slf4j
// https://www.baeldung.com/java-acquire-lock-by-key
public class LockByKeyService<Key> {
    private static class LockWrapper {
        private final Semaphore lock = new Semaphore(1);
        private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

        private LockWrapper addThreadInQueue() {
            numberOfThreadsInQueue.incrementAndGet();
            return this;
        }

        private int removeThreadFromQueue() {
            return numberOfThreadsInQueue.decrementAndGet();
        }
    }

    private final ConcurrentHashMap<Key, LockWrapper> locks = new ConcurrentHashMap<>();

    public void lock(Key key) throws InterruptedException {
        LockWrapper lockWrapper = locks.compute(key, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
        lockWrapper.lock.acquire();
    }

    public void unlock(Key key) {
        LockWrapper lockWrapper = locks.get(key);
        lockWrapper.lock.release();
        if (lockWrapper.removeThreadFromQueue() == 0) {
            locks.remove(key, lockWrapper);
        }
    }
}
