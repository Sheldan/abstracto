package dev.sheldan.abstracto.moderation.config.feature.mode;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum ModerationMode implements FeatureMode {
    BAN_LOG("banLogging"), KICK_LOG("kickLogging");

    private final String key;

    ModerationMode(String key) {
        this.key = key;
    }

}
