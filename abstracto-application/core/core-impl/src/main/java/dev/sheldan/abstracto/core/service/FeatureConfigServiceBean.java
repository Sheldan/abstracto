package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class FeatureConfigServiceBean implements FeatureConfigService {

    @Autowired
    private List<FeatureConfig> availableFeatures;

    @Autowired
    private FeatureValidatorService featureValidatorService;

    @Override
    public List<String> getAllFeatures() {
        return availableFeatures
                .stream()
                .map(featureDisplay -> featureDisplay.getFeature().getKey())
                .collect(Collectors.toList());
    }

    @Override
    public List<FeatureConfig> getAllFeatureConfigs() {
        return availableFeatures;
    }

    @Override
    public FeatureConfig getFeatureDisplayForFeature(FeatureEnum featureEnum) {
        Optional<FeatureConfig> any = getAllFeatureConfigs().stream().filter(featureDisplay -> featureDisplay.getFeature().equals(featureEnum)).findAny();
        if(any.isPresent()) {
            return any.get();
        }
        throw new AbstractoRunTimeException(String.format("Feature %s not found in configuration", featureEnum.getKey()));
    }

    @Override
    public FeatureConfig getFeatureDisplayForFeature(String key) {
        return getFeatureDisplayForFeature(getFeatureEnum(key));
    }

    @Override
    public boolean doesFeatureExist(FeatureConfig name) {
        return availableFeatures.stream().anyMatch(featureDisplay -> featureDisplay.getFeature().equals(name.getFeature()));
    }

    @Override
    public List<String> getFeaturesAsList() {
        return availableFeatures
                .stream()
                .map(featureDisplay -> featureDisplay.getFeature().getKey())
                .collect(Collectors.toList());
    }

    @Override
    public boolean doesFeatureExist(String name) {
        return availableFeatures.stream().anyMatch(featureDisplay -> featureDisplay.getFeature().getKey().equals(name));
    }

    @Override
    public FeatureEnum getFeatureEnum(String key) {
        Optional<FeatureConfig> foundFeature = availableFeatures.stream().filter(featureDisplay -> featureDisplay.getFeature().getKey().equals(key)).findAny();
        if(foundFeature.isPresent()) {
            return foundFeature.get().getFeature();
        }
        throw new AbstractoRunTimeException(String.format("Feature %s not found.", key));
    }

    @Override
    public PostTargetEnum getPostTargetEnumByKey(String key) {
        Predicate<PostTargetEnum> postTargetComparison = postTargetEnum -> postTargetEnum.getKey().equals(key);
        Optional<FeatureConfig> foundFeature = availableFeatures.stream().filter(featureDisplay -> featureDisplay.getRequiredPostTargets().stream().anyMatch(postTargetComparison)).findAny();
        if(foundFeature.isPresent()) {
            return foundFeature.get().getRequiredPostTargets().stream().filter(postTargetComparison).findAny().get();
        }
        throw new AbstractoRunTimeException(String.format("Post target %s not found.", key));
    }

    @Override
    public FeatureValidationResult validateFeatureSetup(FeatureConfig featureConfig, AServer server) {
        FeatureValidationResult featureValidationResult = FeatureValidationResult.validationSuccessful(featureConfig);
        featureConfig.getRequiredPostTargets().forEach(s -> {
            featureValidatorService.checkPostTarget(s, server, featureValidationResult);
        });
        featureConfig.getRequiredSystemConfigKeys().forEach(s -> {
            featureValidatorService.checkSystemConfig(s, server, featureValidationResult);
        });
        featureConfig.getRequiredEmotes().forEach(s -> {
            featureValidatorService.checkEmote(s, server, featureValidationResult);
        });
        featureConfig.getAdditionalFeatureValidators().forEach(featureValidator -> {
            featureValidator.featureIsSetup(featureConfig, server, featureValidationResult);
        });
        return featureValidationResult;
    }

    @Override
    public FeatureMode getFeatureModeByKey(String key) {
        Predicate<FeatureMode> postTargetComparison = postTargetEnum -> postTargetEnum.getKey().equals(key);
        Optional<FeatureConfig> foundFeature = availableFeatures.stream().filter(featureDisplay -> featureDisplay.getAvailableModes().stream().anyMatch(postTargetComparison)).findAny();
        if(foundFeature.isPresent()) {
            return foundFeature.get().getAvailableModes().stream().filter(postTargetComparison).findAny().get();
        }
        throw new AbstractoRunTimeException(String.format("Feature mode %s not found.", key));
    }

    @Override
    public boolean isModeValid(String featureName, String modeName) {
        return availableFeatures
                .stream()
                .filter(featureConfig -> featureConfig.getFeature().getKey().equals(featureName))
                .map(FeatureConfig::getAvailableModes)
                .anyMatch(featureModes -> featureModes
                        .stream()
                        .anyMatch(featureMode -> featureMode.getKey().equals(modeName))
                );
    }
}
