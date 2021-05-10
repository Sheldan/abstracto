package dev.sheldan.abstracto.invitefilter.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum InviteFilterFeatureDefinition  implements FeatureDefinition {
    INVITE_FILTER("inviteFilter");

    private final String key;

    InviteFilterFeatureDefinition(String key) {
        this.key = key;
    }
}
