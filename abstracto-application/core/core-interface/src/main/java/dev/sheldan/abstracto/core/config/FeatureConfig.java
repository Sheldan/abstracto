package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.interactive.SetupStep;
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
    default List<String> getRequiredEmotes() { return Collections.emptyList(); }
    default List<FeatureMode> getAvailableModes() { return Collections.emptyList(); };
    default List<SetupStep> getCustomSetupSteps() { return Collections.emptyList(); }
}
