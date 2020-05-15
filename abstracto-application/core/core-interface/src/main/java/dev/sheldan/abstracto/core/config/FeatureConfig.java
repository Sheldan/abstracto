package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.service.FeatureValidator;

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
    default List<PostTargetEnum> getRequiredPostTargets() { return Collections.emptyList();}
    default List<String> getRequiredSystemConfigKeys() { return Collections.emptyList();}
    default List<FeatureValidator> getAdditionalFeatureValidators() { return Collections.emptyList(); }
}
