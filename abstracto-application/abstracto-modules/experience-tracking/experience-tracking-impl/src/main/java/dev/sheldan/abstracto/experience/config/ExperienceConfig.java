package dev.sheldan.abstracto.experience.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Config file containing the default configurations related to experience
 */
@Configuration
@PropertySource("classpath:experience-config.properties")
public class ExperienceConfig {
}

