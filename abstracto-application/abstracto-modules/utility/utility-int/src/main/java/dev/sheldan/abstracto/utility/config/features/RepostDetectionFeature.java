package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RepostDetectionFeature implements FeatureConfig {
    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REPOST_DETECTION;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("repostMarker");
    }
}
