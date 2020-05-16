package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;

import java.util.Collections;
import java.util.List;

public interface FeatureAware {
    FeatureEnum getFeature();
    default List<FeatureMode> getFeatureModeLimitations() { return Collections.emptyList();}
}
