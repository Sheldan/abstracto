package dev.sheldan.abstracto.twitch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:twitch-config.properties")
public class TwitchConfig {
}

