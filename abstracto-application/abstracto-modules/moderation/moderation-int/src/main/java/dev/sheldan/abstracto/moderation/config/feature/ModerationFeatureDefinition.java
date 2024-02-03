package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum ModerationFeatureDefinition implements FeatureDefinition {
    MODERATION("moderation"),
    WARNING("warnings"),
    MUTING("muting"),
    AUTOMATIC_WARN_DECAY("warnDecay"),
    USER_NOTES("userNotes"),
    REPORT_REACTIONS("reportReactions"),
    INFRACTIONS("infractions"),
    HONEYPOT("honeypot")
    ;

    private final String key;

    ModerationFeatureDefinition(String key) {
        this.key = key;
    }
}
