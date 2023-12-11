package dev.sheldan.abstracto.entertainment.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EntertainmentFeatureConfig implements FeatureConfig {

    public static final String ROULETTE_BULLETS_CONFIG_KEY = "rouletteBullets";
    public static final String ROLL_DEFAULT_HIGH_KEY = "rollDefaultHigh";
    public static final String PRESS_F_DEFAULT_DURATION_SECONDS = "pressFDefaultDurationSeconds";
    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(ROULETTE_BULLETS_CONFIG_KEY, ROLL_DEFAULT_HIGH_KEY, PRESS_F_DEFAULT_DURATION_SECONDS);
    }
}
