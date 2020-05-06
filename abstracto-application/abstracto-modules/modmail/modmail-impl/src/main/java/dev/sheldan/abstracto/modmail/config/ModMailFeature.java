package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

@Component
public class ModMailFeature implements FeatureConfig {
    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MODMAIL;
    }
}
