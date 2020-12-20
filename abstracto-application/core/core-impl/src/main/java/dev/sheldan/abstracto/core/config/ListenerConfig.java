package dev.sheldan.abstracto.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:listenerConfig.properties")
public class ListenerConfig {
}

