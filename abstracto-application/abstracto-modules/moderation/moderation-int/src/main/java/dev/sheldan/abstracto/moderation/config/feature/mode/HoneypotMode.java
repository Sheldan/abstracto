package dev.sheldan.abstracto.moderation.config.feature.mode;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum HoneypotMode implements FeatureMode {
    ROLE("honeypotRole"),
    MESSAGE("honeypotMessage");

    private final String key;

    HoneypotMode(String key) {
        this.key = key;
    }
}
