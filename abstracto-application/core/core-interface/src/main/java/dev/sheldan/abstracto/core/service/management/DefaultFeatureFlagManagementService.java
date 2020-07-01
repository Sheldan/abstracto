package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.DefaultFeatureFlag;

import java.util.List;

public interface DefaultFeatureFlagManagementService {
    List<String> getDefaultFeatureKeys();
    List<DefaultFeatureFlag> getAllDefaultFeatureFlags();
}
