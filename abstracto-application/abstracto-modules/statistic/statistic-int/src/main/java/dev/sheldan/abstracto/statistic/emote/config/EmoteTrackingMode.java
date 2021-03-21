package dev.sheldan.abstracto.statistic.emote.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

/**
 * {@link FeatureMode}s for {@link EmoteTrackingFeatureConfig}. These modes include:
 * AUTO_TRACK: This controls a listener which listens for the emote events of a server, and automatically creates/updates/marks as deleted instances in the database if the respective event happens
 *             Influences:
 * EXTERNAL_EMOTES: Enables the tracking of emotes which are not from the server the feature is enabled in. This feature alone only enables to track emotes with the `trackEmote` command and makes the command `externalEmoteStats` (and more) available
 * AUTO_TRACK_EXTERNAL: Every external emote which is encountered in a message will be tracked (created and updated), only works in combination with EXTERNAL_MOTES
 */
@Getter
public enum  EmoteTrackingMode implements FeatureMode {
    AUTO_TRACK("emoteAutoTrack"), EXTERNAL_EMOTES("externalEmotes"), AUTO_TRACK_EXTERNAL("autoTrackExternal");

    private final String key;

    EmoteTrackingMode(String key) {
        this.key = key;
    }

}
