package dev.sheldan.abstracto.statistic.emotes.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EmoteTrackingFeature implements FeatureConfig {
    @Override
    public FeatureEnum getFeature() {
        return StatisticFeatures.EMOTE_TRACKING;
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(EmoteTrackingMode.EXTERNAL_EMOTES, EmoteTrackingMode.AUTO_TRACK, EmoteTrackingMode.AUTO_TRACK_EXTERNAL);
    }
}
