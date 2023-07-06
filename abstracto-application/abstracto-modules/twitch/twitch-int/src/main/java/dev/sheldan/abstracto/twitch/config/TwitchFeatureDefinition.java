package dev.sheldan.abstracto.twitch.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum TwitchFeatureDefinition implements FeatureDefinition {
    TWITCH("twitch");

    private String key;

    TwitchFeatureDefinition(String key) {
        this.key = key;
    }
}
