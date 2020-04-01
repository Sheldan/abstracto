package dev.sheldan.abstracto.commands.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:commands.properties")
public class CommandConfig {
}

