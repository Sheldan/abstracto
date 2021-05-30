package dev.sheldan.abstracto.moderation.config.posttarget;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum ReactionReportPostTarget implements PostTargetEnum {
    REACTION_REPORTS("reactionReports");

    private String key;

    ReactionReportPostTarget(String key) {
        this.key = key;
    }
}
