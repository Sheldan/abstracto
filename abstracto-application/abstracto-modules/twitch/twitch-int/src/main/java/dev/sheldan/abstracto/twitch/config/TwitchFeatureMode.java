package dev.sheldan.abstracto.twitch.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum TwitchFeatureMode implements FeatureMode {
    DELETE_NOTIFICATION("deleteNotification"), UPDATE_NOTIFICATION("updateNotification");

    private final String key;

    TwitchFeatureMode(String key) {
        this.key = key;
    }

}
