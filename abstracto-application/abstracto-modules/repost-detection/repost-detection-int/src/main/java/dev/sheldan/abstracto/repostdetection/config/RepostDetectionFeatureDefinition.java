package dev.sheldan.abstracto.repostdetection.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum RepostDetectionFeatureDefinition implements FeatureDefinition {
    REPOST_DETECTION("repostDetection");

    private String key;

    RepostDetectionFeatureDefinition(String key) {
        this.key = key;
    }
}
