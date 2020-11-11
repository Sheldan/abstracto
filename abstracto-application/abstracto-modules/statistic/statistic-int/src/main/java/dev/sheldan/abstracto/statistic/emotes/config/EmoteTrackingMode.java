package dev.sheldan.abstracto.statistic.emotes.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum  EmoteTrackingMode implements FeatureMode {
    AUTO_TRACK("emoteAutoTrack"), EXTERNAL_EMOTES("externalEmotes"), AUTO_TRACK_EXTERNAL("autoTrackExternal");

    private final String key;

    EmoteTrackingMode(String key) {
        this.key = key;
    }

}
