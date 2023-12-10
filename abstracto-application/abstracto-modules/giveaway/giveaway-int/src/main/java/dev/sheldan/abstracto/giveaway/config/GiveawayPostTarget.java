package dev.sheldan.abstracto.giveaway.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum GiveawayPostTarget implements PostTargetEnum {
    GIVEAWAYS("giveaways");

    private String key;

    GiveawayPostTarget(String key) {
        this.key = key;
    }
}
