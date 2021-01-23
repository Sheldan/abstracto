package dev.sheldan.abstracto.moderation.config.features.mode;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum  InviteFilterMode implements FeatureMode {
    TRACK_USES("trackUses"), FILTER_NOTIFICATIONS("filterNotifications");

    private final String key;

    InviteFilterMode(String key) {
        this.key = key;
    }

}
