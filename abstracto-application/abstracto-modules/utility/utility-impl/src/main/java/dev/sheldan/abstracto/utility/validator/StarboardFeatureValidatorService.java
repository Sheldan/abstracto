package dev.sheldan.abstracto.utility.validator;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import dev.sheldan.abstracto.utility.StarboardFeatureValidator;
import dev.sheldan.abstracto.utility.config.StarboardConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StarboardFeatureValidatorService implements StarboardFeatureValidator {

    @Autowired
    private StarboardConfig starboardConfig;

    @Autowired
    private FeatureValidatorService featureValidatorService;

    @Override
    public void featureIsSetup(FeatureConfig featureConfig, AServer server, FeatureValidationResult validationResult) {
        for(int i = starboardConfig.getLvl().size(); i > 0; i--) {
            featureValidatorService.checkSystemConfig("starLvl" + i, server, validationResult);
        }
    }
}
