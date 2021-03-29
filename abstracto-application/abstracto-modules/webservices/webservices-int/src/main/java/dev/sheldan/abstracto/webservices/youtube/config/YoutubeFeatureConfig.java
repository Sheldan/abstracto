package dev.sheldan.abstracto.webservices.youtube.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class YoutubeFeatureConfig implements FeatureConfig {
    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.YOUTUBE;
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(YoutubeWebServiceFeatureMode.VIDEO_DETAILS);
    }
}
