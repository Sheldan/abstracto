package dev.sheldan.abstracto.moderation.config.posttargets;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum MutingPostTarget implements PostTargetEnum {
    MUTE_LOG("muteLog");

    private String key;

    MutingPostTarget(String key) {
        this.key = key;
    }
}
