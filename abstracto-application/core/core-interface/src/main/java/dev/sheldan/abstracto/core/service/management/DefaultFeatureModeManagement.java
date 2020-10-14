package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.DefaultFeatureMode;

import java.util.List;
import java.util.Optional;

public interface DefaultFeatureModeManagement {
    List<DefaultFeatureMode> getFeatureModesForFeature(AFeature feature);
    List<DefaultFeatureMode> getAll();
    Optional<DefaultFeatureMode> getFeatureModeOptional(AFeature feature, String mode);
    DefaultFeatureMode getFeatureMode(AFeature feature, String mode);
}
