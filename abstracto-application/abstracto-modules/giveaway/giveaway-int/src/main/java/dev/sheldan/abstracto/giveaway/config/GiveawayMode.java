package dev.sheldan.abstracto.giveaway.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum GiveawayMode implements FeatureMode {
    KEY_GIVEAWAYS("keyGiveaways"),
    AUTO_NOTIFY_GIVEAWAY_KEY_WINNERS("autoNotifyGiveawayKeyWinners");

    private final String key;

    GiveawayMode(String key) {
        this.key = key;
    }

}
