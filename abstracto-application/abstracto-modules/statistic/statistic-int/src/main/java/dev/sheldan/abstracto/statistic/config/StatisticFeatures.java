package dev.sheldan.abstracto.statistic.config;

import dev.sheldan.abstracto.core.config.FeatureEnum;

public enum StatisticFeatures implements FeatureEnum {
    EMOTE_TRACKING("emote_tracking");

    private String key;

    StatisticFeatures(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
