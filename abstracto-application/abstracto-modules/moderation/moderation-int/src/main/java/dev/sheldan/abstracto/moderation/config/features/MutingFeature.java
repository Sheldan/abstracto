package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

@Component
public class MutingFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MUTING;
    }

}
