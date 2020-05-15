package dev.sheldan.abstracto.experience.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.FeatureValidator;
import dev.sheldan.abstracto.experience.validator.ExperienceFeatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ExperienceFeatureConfig implements FeatureConfig {

    @Autowired
    private ExperienceFeatureValidator experienceFeatureValidator;

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }

    @Override
    public List<FeatureValidator> getAdditionalFeatureValidators() {
        return Arrays.asList(experienceFeatureValidator);
    }
}
