package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

/**
 * The experience tracking feature enum, this is used to switch off/on experience tracking.
 */
@Getter
public enum ExperienceFeatureDefinition implements FeatureDefinition {
    EXPERIENCE("experience");

    private String key;

    ExperienceFeatureDefinition(String key) {
        this.key = key;
    }
}
