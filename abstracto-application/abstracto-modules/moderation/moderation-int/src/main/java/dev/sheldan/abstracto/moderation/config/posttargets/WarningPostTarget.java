package dev.sheldan.abstracto.moderation.config.posttargets;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum WarningPostTarget implements PostTargetEnum {
    WARN_LOG("warnLog");

    private String key;

    WarningPostTarget(String key) {
        this.key = key;
    }
}
