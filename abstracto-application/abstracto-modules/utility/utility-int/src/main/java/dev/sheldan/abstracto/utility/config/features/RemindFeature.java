package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import org.springframework.stereotype.Component;

@Component
public class RemindFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REMIND;
    }

}
