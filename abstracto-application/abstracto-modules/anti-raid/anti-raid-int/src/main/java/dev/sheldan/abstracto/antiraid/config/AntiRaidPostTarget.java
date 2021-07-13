package dev.sheldan.abstracto.antiraid.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum AntiRaidPostTarget implements PostTargetEnum {
    MASS_PING_LOG("massPingLog");

    private String key;

    AntiRaidPostTarget(String key) {
        this.key = key;
    }
}
