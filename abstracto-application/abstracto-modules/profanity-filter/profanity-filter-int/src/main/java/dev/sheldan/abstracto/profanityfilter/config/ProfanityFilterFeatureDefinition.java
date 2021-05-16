package dev.sheldan.abstracto.profanityfilter.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum ProfanityFilterFeatureDefinition implements FeatureDefinition {
    PROFANITY_FILTER("profanityFilter");

    private final String key;

    ProfanityFilterFeatureDefinition(String key) {
        this.key = key;
    }
}
