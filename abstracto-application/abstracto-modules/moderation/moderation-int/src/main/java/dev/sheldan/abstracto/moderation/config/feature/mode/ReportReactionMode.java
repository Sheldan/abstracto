package dev.sheldan.abstracto.moderation.config.feature.mode;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum ReportReactionMode implements FeatureMode {
    SINGULAR_MESSAGE("singularReportReactions"), ANONYMOUS("anonymousReportReactions"), REPORT_ACTIONS("reactionReportActions");

    private final String key;

    ReportReactionMode(String key) {
        this.key = key;
    }
}
