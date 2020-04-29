package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.Getter;

@Getter
public enum ModerationFeatures implements FeatureEnum {
    MODERATION("moderation"), WARNING("warnings"), LOGGING("logging"), MUTING("muting"), AUTOMATIC_WARN_DECAY("warnDecay");

    private String key;

    ModerationFeatures(String key) {
        this.key = key;
    }
}
