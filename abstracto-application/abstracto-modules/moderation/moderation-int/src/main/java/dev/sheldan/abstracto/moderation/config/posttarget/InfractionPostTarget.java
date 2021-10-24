package dev.sheldan.abstracto.moderation.config.posttarget;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum InfractionPostTarget implements PostTargetEnum {
    INFRACTION_NOTIFICATION("infractionNotification");

    private String key;

    InfractionPostTarget(String key) {
        this.key = key;
    }
}
