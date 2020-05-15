package dev.sheldan.abstracto.moderation.config.posttargets;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum ModerationPostTarget implements PostTargetEnum {
    KICK_LOG("kickLog"), BAN_LOG("banLog");

    private String key;

    ModerationPostTarget(String key) {
        this.key = key;
    }
}
