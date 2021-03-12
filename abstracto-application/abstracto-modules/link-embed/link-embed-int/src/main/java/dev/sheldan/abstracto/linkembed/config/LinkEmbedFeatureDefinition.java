package dev.sheldan.abstracto.linkembed.config;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import lombok.Getter;

@Getter
public enum LinkEmbedFeatureDefinition implements FeatureDefinition {
    LINK_EMBEDS("linkEmbeds");

    private String key;

    LinkEmbedFeatureDefinition(String key) {
        this.key = key;
    }
}
