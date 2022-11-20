package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.service.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class ExperienceExecutorConfig {

    @Autowired
    private ExecutorService executorService;

    @Bean(name = "experienceUpdateExecutor")
    public TaskExecutor experienceUpdateExecutor() {
        return executorService.setupExecutorFor("experienceUpdateExecutor");
    }
}
