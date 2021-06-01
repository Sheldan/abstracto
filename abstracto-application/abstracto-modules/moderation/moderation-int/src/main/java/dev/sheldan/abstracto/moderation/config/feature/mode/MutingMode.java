package dev.sheldan.abstracto.moderation.config.feature.mode;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum MutingMode implements FeatureMode {
    MANUAL_UN_MUTE_LOGGING("manualUnMuteLogging");

    private final String key;

    MutingMode(String key) {
        this.key = key;
    }

}
