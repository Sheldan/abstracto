package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum  ModMailMode implements FeatureMode {
    LOGGING("log"), NO_LOG("nolog");

    private String key;

    ModMailMode(String key) {
        this.key = key;
    }

}
