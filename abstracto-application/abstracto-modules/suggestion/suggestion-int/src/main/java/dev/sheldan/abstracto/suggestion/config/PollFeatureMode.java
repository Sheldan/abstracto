package dev.sheldan.abstracto.suggestion.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum PollFeatureMode implements FeatureMode {
    POLL_AUTO_EVALUATE("pollAutoEvaluate"),
    POLL_REMINDER("pollReminder");

    private final String key;

    PollFeatureMode(String key) {
        this.key = key;
    }
}
