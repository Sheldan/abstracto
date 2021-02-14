package dev.sheldan.abstracto.utility.validator;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.utility.StarboardFeatureValidator;
import dev.sheldan.abstracto.utility.config.features.StarboardFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StarboardFeatureValidatorService implements StarboardFeatureValidator {

    @Autowired
    private FeatureValidatorService featureValidatorService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public void featureIsSetup(FeatureConfig featureConfig, AServer server, FeatureValidationResult validationResult) {
        int levelAmount = defaultConfigManagementService.getDefaultConfig(StarboardFeature.STAR_LEVELS_CONFIG_KEY).getLongValue().intValue();
        log.info("Validating starboard feature for server {}.", server.getId());
        for(int i = 1; i <= levelAmount; i++) {
            featureValidatorService.checkSystemConfig(StarboardFeature.STAR_LVL_CONFIG_PREFIX + i, server, validationResult);
        }
    }
}
