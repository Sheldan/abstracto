package dev.sheldan.abstracto.entertainment.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GamesFeatureConfig implements FeatureConfig {

    public static final String MINES_CREDITS_FACTOR = "minesCreditsFactor";
    public static final String MINES_MINIMUM_MINES_RATIO = "minesMinMineRatio";

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.GAMES;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(MINES_CREDITS_FACTOR, MINES_MINIMUM_MINES_RATIO);
    }

}
