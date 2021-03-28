package dev.sheldan.abstracto.webservices.youtube.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import org.springframework.stereotype.Component;

@Component
public class YoutubeFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.YOUTUBE;
    }
}
