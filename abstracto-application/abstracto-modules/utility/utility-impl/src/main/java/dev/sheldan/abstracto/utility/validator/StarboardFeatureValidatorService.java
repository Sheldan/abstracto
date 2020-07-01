package dev.sheldan.abstracto.utility.validator;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.utility.StarboardFeatureValidator;
import dev.sheldan.abstracto.utility.service.StarboardServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StarboardFeatureValidatorService implements StarboardFeatureValidator {

    @Autowired
    private FeatureValidatorService featureValidatorService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public void featureIsSetup(FeatureConfig featureConfig, AServer server, FeatureValidationResult validationResult) {
        int levelAmount = defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY).getLongValue().intValue();
        for(int i = 1; i <= levelAmount; i++) {
            featureValidatorService.checkSystemConfig("starLvl" + i, server, validationResult);
        }
    }
}
