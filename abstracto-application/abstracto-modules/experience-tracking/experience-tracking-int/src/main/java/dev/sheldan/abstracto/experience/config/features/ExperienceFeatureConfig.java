package dev.sheldan.abstracto.experience.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ExperienceFeatureConfig implements FeatureConfig {

    public static final String MIN_EXP_KEY = "minExp";
    public static final String MAX_EXP_KEY = "maxExp";
    public static final String EXP_MULTIPLIER_KEY = "expMultiplier";

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(EXP_MULTIPLIER_KEY, MIN_EXP_KEY, MAX_EXP_KEY);
    }
}
