package dev.sheldan.abstracto.remind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class RemindSpringConfig {

    @Bean(name = "reminderScheduler")
    public ScheduledExecutorService getUnMuteExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

}
