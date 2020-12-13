package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static dev.sheldan.abstracto.utility.config.features.UtilityFeature.ENTERTAINMENT;

@Component
public class EntertainmentFeature implements FeatureConfig {

    public static final String ROULETTE_BULLETS_CONFIG_KEY = "rouletteBullets";
    public static final String ROLL_DEFAULT_HIGH_KEY = "rollDefaultHigh";
    @Override
    public FeatureEnum getFeature() {
        return ENTERTAINMENT;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(ROULETTE_BULLETS_CONFIG_KEY, ROLL_DEFAULT_HIGH_KEY);
    }
}
