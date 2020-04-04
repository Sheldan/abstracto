package dev.sheldan.abstracto.core.command.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:commands.properties")
public class CommandConfig {
}

