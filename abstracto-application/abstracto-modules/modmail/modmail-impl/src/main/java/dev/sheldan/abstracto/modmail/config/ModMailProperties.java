package dev.sheldan.abstracto.modmail.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/modmail.properties")
public class ModMailProperties {
}
