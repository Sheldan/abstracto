package dev.sheldan.abstracto.assignableroles.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.Getter;

@Getter
public enum  AssignableRoleFeature implements FeatureEnum {
    ASSIGNABLE_ROLES("assignableRole");

    private String key;

    AssignableRoleFeature(String key) {
        this.key = key;
    }
}
