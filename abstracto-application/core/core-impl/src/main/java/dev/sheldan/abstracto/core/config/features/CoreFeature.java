package dev.sheldan.abstracto.core.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureDisplay;
import org.springframework.stereotype.Component;

@Component
public class CoreFeature implements FeatureDisplay {

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }

}
