package dev.sheldan.abstracto.moderation.config.posttargets;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum WarnDecayPostTarget implements PostTargetEnum {
    DECAY_LOG("decayLog");

    private String key;

    WarnDecayPostTarget(String key) {
        this.key = key;
    }
}
