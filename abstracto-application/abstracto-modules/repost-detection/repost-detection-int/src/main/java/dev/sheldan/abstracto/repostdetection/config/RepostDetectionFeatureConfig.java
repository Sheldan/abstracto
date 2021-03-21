package dev.sheldan.abstracto.repostdetection.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RepostDetectionFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return RepostDetectionFeatureDefinition.REPOST_DETECTION;
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList("repostMarker");
    }
}
