package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.PostTargetValidationError;
import dev.sheldan.abstracto.core.models.SystemConfigValidationError;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureValidationServiceBean implements FeatureValidatorService {

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private ConfigManagementService configService;

    @Override
    public void checkPostTarget(PostTargetEnum name, AServer server, FeatureValidationResult featureValidationResult) {
        if(!postTargetManagement.postTargetExists(name.getKey(), server)) {
            PostTargetValidationError validationError = PostTargetValidationError.builder().postTargetName(name.getKey()).build();
            featureValidationResult.setValidationResult(false);
            featureValidationResult.getValidationErrors().add(validationError);
        }
    }

    @Override
    public void checkSystemConfig(String name, AServer server, FeatureValidationResult featureValidationResult) {
        if(!configService.configExists(server, name)) {
            SystemConfigValidationError validationError = SystemConfigValidationError.builder().configKey(name).build();
            featureValidationResult.setValidationResult(false);
            featureValidationResult.getValidationErrors().add(validationError);
        }
    }



}
