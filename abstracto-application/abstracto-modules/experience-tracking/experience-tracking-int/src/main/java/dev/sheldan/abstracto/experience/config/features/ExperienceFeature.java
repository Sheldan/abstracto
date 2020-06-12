package dev.sheldan.abstracto.experience.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.Getter;

/**
 * The experience tracking feature enum, this is used to switch off/on experience tracking.
 */
@Getter
public enum ExperienceFeature implements FeatureEnum {
    EXPERIENCE("experience");

    private String key;

    ExperienceFeature(String key) {
        this.key = key;
    }
}
