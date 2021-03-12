package dev.sheldan.abstracto.assignableroles.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum AssignableRoleFeatureDefinition implements FeatureDefinition {
    ASSIGNABLE_ROLES("assignableRole");

    private String key;

    AssignableRoleFeatureDefinition(String key) {
        this.key = key;
    }
}
