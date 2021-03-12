package dev.sheldan.abstracto.core.command.config.features;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum CoreFeatureDefinition implements FeatureDefinition {
    CORE_FEATURE("core");

    private String key;

    CoreFeatureDefinition(String key) {
        this.key = key;
    }
}
