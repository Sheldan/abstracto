package dev.sheldan.abstracto.webservices.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum WebserviceFeatureDefinition implements FeatureDefinition {
    YOUTUBE("youtube"),
    URBAN_DICTIONARY("urban"),
    THREAD_READER("threadReader"),
    OPEN_WEATHER_MAP("openWeatherMap");

    private String key;

    WebserviceFeatureDefinition(String key) {
        this.key = key;
    }
}
