package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.exception.FeatureNotFoundException;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.listener.FeatureAwareListenerModel;
import dev.sheldan.abstracto.core.listener.ListenerExecutionResult;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FeatureConfigServiceBean implements FeatureConfigService {

    @Autowired
    private List<FeatureConfig> availableFeatures;

    @Autowired
    private FeatureValidatorService featureValidatorService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureModeService featureModeService;

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
    public FeatureConfig getFeatureDisplayForFeature(FeatureDefinition featureDefinition) {
        Optional<FeatureConfig> any = getAllFeatureConfigs().stream().filter(featureDisplay -> featureDisplay.getFeature().equals(featureDefinition)).findAny();
        if(any.isPresent()) {
            return any.get();
        }
        throw new FeatureNotFoundException(featureDefinition.getKey(), getFeaturesAsList());
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
    public List<String> getFeatureModesFromFeatureAsString(String featureName) {
        FeatureConfig featureConfig = getFeatureDisplayForFeature(featureName);
        return featureConfig.getAvailableModes().stream().map(FeatureMode::getKey).collect(Collectors.toList());
    }

    @Override
    public boolean doesFeatureExist(String name) {
        return availableFeatures.stream().anyMatch(featureDisplay -> featureDisplay.getFeature().getKey().equalsIgnoreCase(name));
    }

    @Override
    public FeatureDefinition getFeatureEnum(String key) {
        Optional<FeatureConfig> foundFeature = availableFeatures.stream().filter(featureDisplay -> featureDisplay.getFeature().getKey().equalsIgnoreCase(key)).findAny();
        if(foundFeature.isPresent()) {
            return foundFeature.get().getFeature();
        }
        throw new FeatureNotFoundException(key, getFeaturesAsList());
    }

    @Override
    public PostTargetEnum getPostTargetEnumByKey(String key) {
        Predicate<PostTargetEnum> postTargetComparison = postTargetEnum -> postTargetEnum.getKey().equalsIgnoreCase(key);
        Optional<FeatureConfig> foundFeature = availableFeatures.stream().filter(featureDisplay -> featureDisplay.getRequiredPostTargets().stream().anyMatch(postTargetComparison)).findAny();
        if(foundFeature.isPresent()) {
            Optional<PostTargetEnum> any = foundFeature.get().getRequiredPostTargets().stream().filter(postTargetComparison).findAny();
            return any.orElse(null);
        }
        throw new AbstractoRunTimeException(String.format("Post target %s not found.", key));
    }

    @Override
    public FeatureValidationResult validateFeatureSetup(FeatureConfig featureConfig, AServer server) {
        log.info("Verifying feature setup for feature {} in server {}.", featureConfig.getFeature().getKey(), server.getId());
        FeatureValidationResult featureValidationResult = FeatureValidationResult.validationSuccessful(featureConfig);
        featureConfig.getRequiredPostTargets().forEach(s -> {
            log.debug("Checking post target {}.", s.getKey());
            featureValidatorService.checkPostTarget(s, server, featureValidationResult);
        });
        featureConfig.getRequiredSystemConfigKeys().forEach(s -> {
            log.debug("Checking system config key {}.", s);
            featureValidatorService.checkSystemConfig(s, server, featureValidationResult);
        });
        featureConfig.getRequiredEmotes().forEach(s -> {
            log.debug("Checking required emote {}.", s);
            featureValidatorService.checkEmote(s, server, featureValidationResult);
}       );
        featureConfig.getAdditionalFeatureValidators().forEach(featureValidator ->  {
            log.debug("Executing additional feature validator {}.", featureValidator.getClass().getName());
            featureValidator.featureIsSetup(featureConfig, server, featureValidationResult);
        });
        return featureValidationResult;
    }

    @Override
    public FeatureMode getFeatureModeByKey(FeatureConfig featureConfig, String key) {
        Optional<FeatureMode> foundFeature = featureConfig.getAvailableModes().stream().filter(featureMode -> featureMode.getKey().equalsIgnoreCase(key)).findAny();
        if(foundFeature.isPresent()) {
            return foundFeature.get();
        }
        throw new FeatureModeNotFoundException(key, getFeatureModesFromFeatureAsString(featureConfig.getFeature().getKey()));
    }

    @Override
    public FeatureConfig getFeatureConfigForFeature(AFeature feature) {
        return getFeatureDisplayForFeature(feature.getKey());
    }

    @Override
    public boolean isModeValid(String featureName, String modeName) {
        return availableFeatures
                .stream()
                .filter(featureConfig -> featureConfig.getFeature().getKey().equalsIgnoreCase(featureName))
                .map(FeatureConfig::getAvailableModes)
                .anyMatch(featureModes -> featureModes
                        .stream()
                        .anyMatch(featureMode -> featureMode.getKey().equalsIgnoreCase(modeName))
                );
    }

    @Override
    public <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult>  boolean isFeatureAwareEnabled(FeatureAwareListener<T, R> listener, Long serverId) {
        FeatureConfig feature = getFeatureDisplayForFeature(listener.getFeature());
        if(serverId == null) {
            return true;
        }
        if (!featureFlagService.isFeatureEnabled(feature, serverId)) {
            return false;
        }
        return featureModeService.necessaryFeatureModesMet(listener, serverId);
    }
}
