package dev.sheldan.abstracto.remind.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum RemindFeatureDefinition implements FeatureDefinition {
    REMIND("remind");

    private String key;

    RemindFeatureDefinition(String key) {
        this.key = key;
    }
}
