package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

@Component
public class SuggestionFeature implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.SUGGEST;
    }

}
