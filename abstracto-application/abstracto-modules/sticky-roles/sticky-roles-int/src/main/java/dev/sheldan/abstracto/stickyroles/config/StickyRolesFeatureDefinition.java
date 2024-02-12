package dev.sheldan.abstracto.stickyroles.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum StickyRolesFeatureDefinition implements FeatureDefinition {
    STICKY_ROLES("stickyRoles");

    private String key;

    StickyRolesFeatureDefinition(String key) {
        this.key = key;
    }
}
