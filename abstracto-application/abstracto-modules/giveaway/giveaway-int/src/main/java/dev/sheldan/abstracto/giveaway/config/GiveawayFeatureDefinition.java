package dev.sheldan.abstracto.giveaway.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum GiveawayFeatureDefinition implements FeatureDefinition {
    GIVEAWAY("giveaway");

    private String key;

    GiveawayFeatureDefinition(String key) {
        this.key = key;
    }
}
