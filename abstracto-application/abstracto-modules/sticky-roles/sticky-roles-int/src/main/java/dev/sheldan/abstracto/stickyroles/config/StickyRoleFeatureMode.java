package dev.sheldan.abstracto.stickyroles.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum StickyRoleFeatureMode implements FeatureMode {
    ALLOW_SELF_MANAGEMENT("allowSelfManagement");

    private final String key;

    StickyRoleFeatureMode(String key) {
        this.key = key;
    }
}
