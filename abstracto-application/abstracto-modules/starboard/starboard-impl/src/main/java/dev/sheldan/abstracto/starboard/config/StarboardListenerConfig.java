package dev.sheldan.abstracto.starboard.config;

import dev.sheldan.abstracto.core.service.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class StarboardListenerConfig {
    @Autowired
    private ExecutorService executorService;

    @Bean(name = "starboardCreatedListenerExecutor")
    public TaskExecutor starboardPostCreatedExecutor() {
        return executorService.setupExecutorFor("starboardCreatedListener");
    }

    @Bean(name = "starboardDeletedListenerExecutor")
    public TaskExecutor starboardPostDeletedExecutor() {
        return executorService.setupExecutorFor("starboardDeletedListener");
    }
}
