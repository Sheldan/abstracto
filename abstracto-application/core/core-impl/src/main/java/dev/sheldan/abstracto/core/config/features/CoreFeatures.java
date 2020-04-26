package dev.sheldan.abstracto.core.config.features;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import lombok.Getter;

@Getter
public enum CoreFeatures implements FeatureEnum {
    CORE_FEATURE("core");

    private String key;

    CoreFeatures(String key) {
        this.key = key;
    }
}
