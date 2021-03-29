package dev.sheldan.abstracto.webservices.youtube.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum YoutubeWebServiceFeatureMode implements FeatureMode {
    VIDEO_DETAILS("videoDetails");

    private final String key;

    YoutubeWebServiceFeatureMode(String key) {
        this.key = key;
    }
}
