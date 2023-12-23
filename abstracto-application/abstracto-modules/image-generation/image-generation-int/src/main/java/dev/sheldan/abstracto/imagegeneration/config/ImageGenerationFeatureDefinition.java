package dev.sheldan.abstracto.imagegeneration.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum ImageGenerationFeatureDefinition implements FeatureDefinition {
    IMAGE_GENERATION("imageGeneration");

    private String key;

    ImageGenerationFeatureDefinition(String key) {
        this.key = key;
    }
}
