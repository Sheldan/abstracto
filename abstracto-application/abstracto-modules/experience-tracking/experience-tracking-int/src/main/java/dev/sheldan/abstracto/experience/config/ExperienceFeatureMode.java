package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum ExperienceFeatureMode implements FeatureMode {
    LEVEL_UP_NOTIFICATION("levelUpNotification");

    private final String key;

    ExperienceFeatureMode(String key) {
        this.key = key;
    }
}
