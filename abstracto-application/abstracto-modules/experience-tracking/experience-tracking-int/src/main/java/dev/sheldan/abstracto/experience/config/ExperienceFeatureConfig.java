package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * {@link FeatureConfig} instance containing the required configuration concerning system config and post targets for
 * the {@link ExperienceFeatureDefinition} feature.
 */
@Component
public class ExperienceFeatureConfig implements FeatureConfig {

    /**
     * Minimum experience a user can earn per tracked message
     */
    public static final String MIN_EXP_KEY = "minExp";
    /**
     * Maximum experience a user can earn per tracked message
     */
    public static final String MAX_EXP_KEY = "maxExp";
    /**
     * The multiplier which is applied to each calculated gained experience
     */
    public static final String EXP_MULTIPLIER_KEY = "expMultiplier";

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

    /**
     * All of the configuration keys are required in order for this feature to function
     */
    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(EXP_MULTIPLIER_KEY, MIN_EXP_KEY, MAX_EXP_KEY);
    }
}
