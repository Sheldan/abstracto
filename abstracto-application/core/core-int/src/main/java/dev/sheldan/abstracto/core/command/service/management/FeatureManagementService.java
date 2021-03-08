package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.models.database.AFeature;

public interface FeatureManagementService {
    AFeature createFeature(String key);
    boolean featureExists(String key);
    AFeature getFeature(String key);
}
