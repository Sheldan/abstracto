package dev.sheldan.abstracto.moderation.config.features.mode;


import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum WarnDecayMode implements FeatureMode {
    AUTOMATIC_WARN_DECAY_LOG("automaticWarnDecayLogging");

    private final String key;

    WarnDecayMode(String key) {
        this.key = key;
    }

}