package dev.sheldan.abstracto.linkembed.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum LinkEmbedFeatureMode implements FeatureMode {
    DELETE_BUTTON("messageEmbedDeleteButton");

    private final String key;

    LinkEmbedFeatureMode(String key) {
        this.key = key;
    }
}
