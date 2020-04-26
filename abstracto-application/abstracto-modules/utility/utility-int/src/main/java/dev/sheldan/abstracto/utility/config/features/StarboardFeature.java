package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureDisplay;
import org.springframework.stereotype.Component;

@Component
public class StarboardFeature implements FeatureDisplay {

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.STARBOARD;
    }

}
