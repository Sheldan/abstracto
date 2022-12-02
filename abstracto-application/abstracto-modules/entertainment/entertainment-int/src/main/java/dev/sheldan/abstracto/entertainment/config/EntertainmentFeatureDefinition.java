package dev.sheldan.abstracto.entertainment.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum EntertainmentFeatureDefinition implements FeatureDefinition {
    ENTERTAINMENT("entertainment"), ECONOMY("economy"), GAMES("games");

    private String key;

    EntertainmentFeatureDefinition(String key) {
        this.key = key;
    }
}
