package dev.sheldan.abstracto.suggestion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:suggestion-config.properties")
public class SuggestionConfig {
}

