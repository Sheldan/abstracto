package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;

public enum ModMailFeatureDefinition implements FeatureDefinition {
    MOD_MAIL("modmail");

    private String key;

    ModMailFeatureDefinition(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
