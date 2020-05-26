package dev.sheldan.abstracto.scheduling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Loads the property file responsible to configure the scheduling application.
 * This contains elements like database connection configuration, whether or not the tables should be created.
 * or the amount of threads.
 */
@Configuration
@PropertySource("classpath:scheduling.properties")
public class SchedulingProperties {
}