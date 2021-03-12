package dev.sheldan.abstracto.statistic.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;

/**
 * Features available in the statistic module.
 */
public enum StatisticFeatureDefinition implements FeatureDefinition {
    /**
     * Feature responsible to track the emotes used in a message on a server.
     */
    EMOTE_TRACKING("emoteTracking");

    private final String key;

    StatisticFeatureDefinition(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
