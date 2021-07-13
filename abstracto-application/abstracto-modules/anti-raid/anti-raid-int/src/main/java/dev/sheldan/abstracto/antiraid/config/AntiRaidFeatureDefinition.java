package dev.sheldan.abstracto.antiraid.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum AntiRaidFeatureDefinition implements FeatureDefinition {
    ANTI_RAID("antiRaid");

    private String key;

    AntiRaidFeatureDefinition(String key) {
        this.key = key;
    }
}
