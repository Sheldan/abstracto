package dev.sheldan.abstracto.customcommand.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:custom-command-config.properties")
public class CustomCommandConfig {
}

