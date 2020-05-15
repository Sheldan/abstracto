package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import dev.sheldan.abstracto.utility.StarboardFeatureValidator;
import dev.sheldan.abstracto.utility.config.posttargets.StarboardPostTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class StarboardFeature implements FeatureConfig {

    @Autowired
    private StarboardFeatureValidator starboardFeatureValidator;

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.STARBOARD;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(StarboardPostTarget.STARBOARD);
    }

    @Override
    public List<FeatureValidator> getAdditionalFeatureValidators() {
        return Arrays.asList(starboardFeatureValidator);
    }
}
