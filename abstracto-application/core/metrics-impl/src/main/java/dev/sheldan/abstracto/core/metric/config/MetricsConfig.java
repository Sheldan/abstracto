package dev.sheldan.abstracto.core.metric.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:metrics.properties")
public class MetricsConfig {
}