package dev.sheldan.abstracto.core.config;

import java.util.Collections;
import java.util.List;

public interface FeatureConfig {
    FeatureEnum getFeature();
    default List<FeatureConfig> getRequiredFeatures() {
        return Collections.emptyList();
    }
    default List<FeatureConfig> getDependantFeatures() {
        return Collections.emptyList();
    }
}
