package dev.sheldan.abstracto.customcommand.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum CustomCommandFeatureDefinition implements FeatureDefinition {
    CUSTOM_COMMAND("customCommand");

    private String key;

    CustomCommandFeatureDefinition(String key) {
        this.key = key;
    }
}
