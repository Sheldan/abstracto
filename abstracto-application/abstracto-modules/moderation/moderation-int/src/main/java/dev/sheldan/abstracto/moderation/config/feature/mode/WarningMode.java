package dev.sheldan.abstracto.moderation.config.feature.mode;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum WarningMode implements FeatureMode {
    WARN_DECAY_LOG("warnDecayLogging");

    private final String key;

    WarningMode(String key) {
        this.key = key;
    }

}