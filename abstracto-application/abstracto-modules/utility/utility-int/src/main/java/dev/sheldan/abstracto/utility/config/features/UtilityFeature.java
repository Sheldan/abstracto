package dev.sheldan.abstracto.utility.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.Getter;

@Getter
public enum UtilityFeature implements FeatureEnum {
    REMIND("remind"), STARBOARD("starboard"), SUGGEST("suggestion"), UTILITY("utility"), LINK_EMBEDS("link_embeds");

    private String key;

    UtilityFeature(String key) {
        this.key = key;
    }
}
