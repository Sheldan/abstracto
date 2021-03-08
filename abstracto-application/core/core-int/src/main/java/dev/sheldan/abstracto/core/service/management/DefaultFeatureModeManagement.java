package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.property.FeatureModeProperty;

import java.util.List;
import java.util.Optional;

public interface DefaultFeatureModeManagement {
    List<FeatureModeProperty> getFeatureModesForFeature(AFeature feature);
    List<FeatureModeProperty> getAll();
    Optional<FeatureModeProperty> getFeatureModeOptional(AFeature feature, String mode);
    FeatureModeProperty getFeatureMode(AFeature feature, String mode);
}
