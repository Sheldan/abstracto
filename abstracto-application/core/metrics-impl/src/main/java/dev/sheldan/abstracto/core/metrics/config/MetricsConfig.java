package dev.sheldan.abstracto.core.metrics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:metrics.properties")
public class MetricsConfig {
}