package dev.sheldan.abstracto.experience.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.Getter;

@Getter
public enum ExperienceFeature implements FeatureEnum {
    EXPERIENCE("experience");

    private String key;

    ExperienceFeature(String key) {
        this.key = key;
    }
}
