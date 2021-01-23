package dev.sheldan.abstracto.moderation.config.features.mode;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum MutingMode implements FeatureMode {
    MUTE_LOGGING("muteLogging"), UN_MUTE_LOGGING("unMuteLogging"), MANUAL_UN_MUTE_LOGGING("manualUnMuteLogging");

    private final String key;

    MutingMode(String key) {
        this.key = key;
    }

}
