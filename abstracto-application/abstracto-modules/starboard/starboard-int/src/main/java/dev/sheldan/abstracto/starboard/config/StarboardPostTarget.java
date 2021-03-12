package dev.sheldan.abstracto.starboard.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum StarboardPostTarget implements PostTargetEnum {
    STARBOARD("starboard");

    private String key;

    StarboardPostTarget(String key) {
        this.key = key;
    }
}
