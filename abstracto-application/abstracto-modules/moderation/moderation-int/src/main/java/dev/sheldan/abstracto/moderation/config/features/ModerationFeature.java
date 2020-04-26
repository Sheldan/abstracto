package dev.sheldan.abstracto.moderation.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureDisplay;
import org.springframework.stereotype.Component;

@Component
public class ModerationFeature implements FeatureDisplay {

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MODERATION;
    }

}
