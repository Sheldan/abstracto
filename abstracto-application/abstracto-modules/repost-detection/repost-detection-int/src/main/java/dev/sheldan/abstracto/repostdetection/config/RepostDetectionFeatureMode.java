package dev.sheldan.abstracto.repostdetection.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum  RepostDetectionFeatureMode implements FeatureMode {
    DOWNLOAD("download"), LEADERBOARD("leaderboard");

    private final String key;

    RepostDetectionFeatureMode(String key) {
        this.key = key;
    }
}
