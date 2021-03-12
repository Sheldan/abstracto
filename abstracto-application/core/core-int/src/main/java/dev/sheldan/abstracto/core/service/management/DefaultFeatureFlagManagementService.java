package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;

import java.util.List;

public interface DefaultFeatureFlagManagementService {
    List<String> getDefaultFeatureKeys();
    FeatureFlagProperty getDefaultFeatureFlagProperty(AFeature feature);
    FeatureFlagProperty getDefaultFeatureFlagProperty(FeatureDefinition feature);
    List<FeatureFlagProperty> getAllDefaultFeatureFlags();
}
