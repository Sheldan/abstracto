package dev.sheldan.abstracto.logging.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:logging-config.properties")
public class LoggingProperties {
}
