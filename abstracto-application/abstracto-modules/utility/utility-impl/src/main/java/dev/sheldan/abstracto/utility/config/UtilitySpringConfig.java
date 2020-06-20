package dev.sheldan.abstracto.utility.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class UtilitySpringConfig {

    @Bean(name = "reminderScheduler")
    public ScheduledExecutorService getUnMuteExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

}
