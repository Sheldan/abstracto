package dev.sheldan.abstracto.twitch.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum TwitchPostTarget implements PostTargetEnum {
    TWITCH_LIVE_NOTIFICATION("twitchLiveNotification");

    private String key;

    TwitchPostTarget(String key) {
        this.key = key;
    }
}
