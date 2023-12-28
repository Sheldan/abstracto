package dev.sheldan.abstracto.webservices.wikipedia.config;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WikipediaFeatureConfig implements FeatureConfig {

    public static final String WIKIPEDIA_LANGUAGE_KEY_SYSTEM_CONFIG_KEY = "wikipediaLanguageKey";
    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.WIKIPEDIA;
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(WIKIPEDIA_LANGUAGE_KEY_SYSTEM_CONFIG_KEY);
    }
}
