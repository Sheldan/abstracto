package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface FeatureValidatorService {
    void checkPostTarget(PostTargetEnum postTargetEnum, AServer server, FeatureValidationResult featureValidationResult);
    boolean checkSystemConfig(String name, AServer server, FeatureValidationResult featureValidationResult);
    void checkEmote(String emoteKey, AServer server, FeatureValidationResult featureValidationResult);
}
