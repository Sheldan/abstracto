package dev.sheldan.abstracto.invitefilter.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum InviteFilterMode implements FeatureMode {
    TRACK_USES("trackUses"), FILTER_NOTIFICATIONS("filterNotifications"), FILTER_MODERATION_ACTIONS("filterModerationActions");

    private final String key;

    InviteFilterMode(String key) {
        this.key = key;
    }

}
