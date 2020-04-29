package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import org.springframework.stereotype.Component;

@Component
public class ModerationFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MODERATION;
    }

}
