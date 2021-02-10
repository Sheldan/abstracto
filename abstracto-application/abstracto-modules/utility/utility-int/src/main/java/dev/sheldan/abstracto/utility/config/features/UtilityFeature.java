package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.Getter;

@Getter
public enum UtilityFeature implements FeatureEnum {
    REMIND("remind"), STARBOARD("starboard"), SUGGEST("suggestion"), UTILITY("utility"),
    LINK_EMBEDS("linkEmbeds"), REPOST_DETECTION("repostDetection"), ENTERTAINMENT("entertainment");

    private String key;

    UtilityFeature(String key) {
        this.key = key;
    }
}
