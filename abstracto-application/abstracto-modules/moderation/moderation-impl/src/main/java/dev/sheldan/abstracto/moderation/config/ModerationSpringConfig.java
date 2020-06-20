package dev.sheldan.abstracto.moderation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ModerationSpringConfig {

    @Bean(name = "unmuteScheduler")
    public ScheduledExecutorService getUnMuteExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

}
