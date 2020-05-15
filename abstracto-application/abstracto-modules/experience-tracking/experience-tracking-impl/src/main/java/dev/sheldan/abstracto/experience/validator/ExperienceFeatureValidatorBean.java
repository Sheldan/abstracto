package dev.sheldan.abstracto.experience.validator;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExperienceFeatureValidatorBean implements ExperienceFeatureValidator {

    @Autowired
    private FeatureValidatorService featureValidatorService;

    @Override
    public void featureIsSetup(FeatureConfig featureConfig, AServer server, FeatureValidationResult validationResult) {
        featureValidatorService.checkSystemConfig("expMultiplier", server, validationResult);
        featureValidatorService.checkSystemConfig("minExp", server, validationResult);
        featureValidatorService.checkSystemConfig("maxExp", server, validationResult);
    }
}
