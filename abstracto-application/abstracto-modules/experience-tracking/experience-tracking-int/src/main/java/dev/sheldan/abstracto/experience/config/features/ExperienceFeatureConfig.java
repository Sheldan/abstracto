package dev.sheldan.abstracto.experience.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ExperienceFeatureConfig implements FeatureConfig {

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList("expMultiplier", "minExp", "maxExp");
    }
}
