package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeatureModeServiceBean implements FeatureModeService {

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureModeManagementService featureModeManagementService;

    @Override
    public AFeatureMode setModeForFeatureTo(String key, AServer server, String newMode) {
       FeatureEnum featureEnum = featureConfigService.getFeatureEnum(key);
       return setModeForFeatureTo(featureEnum, server, newMode);
    }

    @Override
    public AFeatureMode setModeForFeatureTo(AFeatureFlag flag, String newMode) {
        FeatureMode featureMode = featureConfigService.getFeatureModeByKey(newMode);
        return setModeForFeatureTo(flag, featureMode);
    }

    @Override
    public AFeatureMode setModeForFeatureTo(FeatureEnum featureEnum, AServer server, String newMode) {
        FeatureMode featureMode = featureConfigService.getFeatureModeByKey(newMode);
        return setModeForFeatureTo(featureEnum, server, featureMode);
    }

    @Override
    public AFeatureMode setModeForFeatureTo(AFeature feature, AServer server, String newMode) {
        FeatureMode featureMode = featureConfigService.getFeatureModeByKey(newMode);
        return setModeForFeatureTo(feature, server, featureMode);
    }

    @Override
    public AFeatureMode setModeForFeatureTo(FeatureEnum featureEnum, AServer server, FeatureMode mode) {
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        return setModeForFeatureTo(feature, server, mode);
    }

    @Override
    public AFeatureMode setModeForFeatureTo(AFeature feature, AServer server, FeatureMode mode) {
        AFeatureFlag featureFlag = featureFlagManagementService.getFeatureFlag(feature, server);
        return setModeForFeatureTo(featureFlag, mode);
    }

    @Override
    public AFeatureMode setModeForFeatureTo(AFeatureFlag featureFlag, FeatureMode mode) {
        return featureModeManagementService.setModeForFeature(featureFlag, mode);
    }


    @Override
    public AFeatureMode createMode(String key, AServer server, String newMode) {
        FeatureEnum featureEnum = featureConfigService.getFeatureEnum(key);
        return createMode(featureEnum, server, newMode);
    }

    @Override
    public AFeatureMode createMode(AFeatureFlag flag, String newMode) {
        FeatureMode featureMode = featureConfigService.getFeatureModeByKey(newMode);
        return createMode(flag, featureMode);
    }

    @Override
    public AFeatureMode createMode(FeatureEnum featureEnum, AServer server, String newMode) {
        FeatureMode featureMode = featureConfigService.getFeatureModeByKey(newMode);
        return createMode(featureEnum, server, featureMode);
    }

    @Override
    public AFeatureMode createMode(AFeature feature, AServer server, String newMode) {
        FeatureMode featureMode = featureConfigService.getFeatureModeByKey(newMode);
        return createMode(feature, server, featureMode);
    }

    @Override
    public AFeatureMode createMode(FeatureEnum featureEnum, AServer server, FeatureMode mode) {
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        return createMode(feature, server, mode);
    }

    @Override
    public AFeatureMode createMode(AFeature feature, AServer server, FeatureMode mode) {
        AFeatureFlag featureFlag = featureFlagManagementService.getFeatureFlag(feature, server);
        return createMode(featureFlag, mode);
    }

    @Override
    public AFeatureMode createMode(AFeatureFlag featureFlag, FeatureMode mode) {
        return featureModeManagementService.createMode(featureFlag, mode);
    }


}
