package dev.sheldan.abstracto.suggestion.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum SuggestionFeatureDefinition implements FeatureDefinition {
    SUGGEST("suggestion");

    private String key;

    SuggestionFeatureDefinition(String key) {
        this.key = key;
    }
}
