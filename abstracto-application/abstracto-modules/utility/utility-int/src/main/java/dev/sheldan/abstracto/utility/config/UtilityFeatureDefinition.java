package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum UtilityFeatureDefinition implements FeatureDefinition {
    UTILITY("utility");

    private String key;

    UtilityFeatureDefinition(String key) {
        this.key = key;
    }
}
