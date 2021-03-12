package dev.sheldan.abstracto.starboard.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum StarboardFeatureDefinition implements FeatureDefinition {
    STARBOARD("starboard");

    private String key;

    StarboardFeatureDefinition(String key) {
        this.key = key;
    }
}
