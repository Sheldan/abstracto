package dev.sheldan.abstracto.logging.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum LoggingFeatureDefinition implements FeatureDefinition {
    LOGGING("logging");

    private final String key;

    LoggingFeatureDefinition(String key) {
        this.key = key;
    }
}
