package dev.sheldan.abstracto.scheduling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:scheduling.properties")
public class SchedulingProperties {
}