package dev.sheldan.abstracto.moderation.config;

import dev.sheldan.abstracto.core.service.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class ModerationListenerConfig {
    @Autowired
    private ExecutorService executorService;

    @Bean(name = "infractionLevelChangedExecutor")
    public TaskExecutor infractionLevelChangedExecutor() {
        return executorService.setupExecutorFor("infractionLevelChangedListener");
    }

    @Bean(name = "warningCreatedExecutor")
    public TaskExecutor warningCreatedExecutor() {
        return executorService.setupExecutorFor("warningCreatedListener");
    }

    @Bean(name = "reportMessageCreatedExecutor")
    public TaskExecutor reportMessageCreatedExecutor() {
        return executorService.setupExecutorFor("reportMessageCreatedListener");
    }

}
