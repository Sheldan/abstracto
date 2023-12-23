package dev.sheldan.abstracto.imagegeneration.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

@Component
public class ImageGenerationFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return ImageGenerationFeatureDefinition.IMAGE_GENERATION;
    }

}
