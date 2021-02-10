package dev.sheldan.abstracto.utility.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:utility-config.properties")
public class UtilityConfig {
}

