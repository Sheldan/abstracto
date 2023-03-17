package dev.sheldan.abstracto.webservices.openweathermap.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OpenWeatherMapConfig implements FeatureConfig {

    public static final String OPEN_WEATHER_MAP_LANGUAGE_KEY_SYSTEM_CONFIG_KEY = "openWeatherMapLanguageKey";

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.OPEN_WEATHER_MAP;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(OPEN_WEATHER_MAP_LANGUAGE_KEY_SYSTEM_CONFIG_KEY);
    }
}
