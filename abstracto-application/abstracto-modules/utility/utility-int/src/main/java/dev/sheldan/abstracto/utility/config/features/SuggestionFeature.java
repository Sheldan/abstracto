package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureDisplay;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

@Component
public class SuggestionFeature implements FeatureDisplay {

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.SUGGEST;
    }

}
