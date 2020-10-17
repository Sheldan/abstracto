package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum WarningMode implements FeatureMode {
    WARN_LOG("warnLogging"), WARN_DECAY_LOG("warnDecayLogging");

    private final String key;

    WarningMode(String key) {
        this.key = key;
    }

}