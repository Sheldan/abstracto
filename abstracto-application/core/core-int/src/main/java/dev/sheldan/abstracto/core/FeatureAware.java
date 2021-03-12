package dev.sheldan.abstracto.core;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;

import java.util.Collections;
import java.util.List;

public interface FeatureAware {
    FeatureDefinition getFeature();
    default List<FeatureMode> getFeatureModeLimitations() { return Collections.emptyList();}
}
