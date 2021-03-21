package dev.sheldan.abstracto.core.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public interface ExecutorService {
    ThreadPoolTaskExecutor setupExecutorFor(String listenerName);
}
