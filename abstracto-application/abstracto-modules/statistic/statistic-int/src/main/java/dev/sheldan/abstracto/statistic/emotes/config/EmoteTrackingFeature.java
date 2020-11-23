package dev.sheldan.abstracto.statistic.emotes.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * {@link FeatureConfig} implementation to define the EmoteTracking feature.
 */
@Component
public class EmoteTrackingFeature implements FeatureConfig {

    /**
     * {@link FeatureEnum} represents the feature uniquely
     */
    @Override
    public FeatureEnum getFeature() {
        return StatisticFeatures.EMOTE_TRACKING;
    }

    /**
     * This feature contains three feature modes. For explanation of them check {@link EmoteTrackingFeature}
     * @return list of {@link FeatureMode} handled by this feature.
     */
    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(EmoteTrackingMode.EXTERNAL_EMOTES, EmoteTrackingMode.AUTO_TRACK, EmoteTrackingMode.AUTO_TRACK_EXTERNAL);
    }
}
