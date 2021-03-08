package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface FeatureValidator {
    void featureIsSetup(FeatureConfig featureConfig, AServer server, FeatureValidationResult validationResult);
}
