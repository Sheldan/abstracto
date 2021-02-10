package dev.sheldan.abstracto.statistic.config;

import dev.sheldan.abstracto.core.config.FeatureEnum;

/**
 * Features available in the statistic module.
 */
public enum StatisticFeatures implements FeatureEnum {
    /**
     * Feature responsible to track the emotes used in a message on a server.
     */
    EMOTE_TRACKING("emoteTracking");

    private final String key;

    StatisticFeatures(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
