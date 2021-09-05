package dev.sheldan.abstracto.suggestion.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum SuggestionFeatureMode implements FeatureMode {
    SUGGESTION_REMINDER("suggestionReminder"), SUGGESTION_BUTTONS("suggestionButton");

    private final String key;

    SuggestionFeatureMode(String key) {
        this.key = key;
    }
}
