package dev.sheldan.abstracto.starboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:starboard-config.properties")
public class StarboardConfig {
}

