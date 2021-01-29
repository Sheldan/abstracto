package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.command.exception.IncorrectFeatureModeException;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureModeManagement;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    @Autowired
    private DefaultFeatureModeManagement defaultFeatureModeManagement;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public void enableFeatureModeForFeature(FeatureEnum featureEnum, AServer server, FeatureMode mode) {
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        Optional<AFeatureMode> existing = featureModeManagementService.getFeatureMode(feature, server, mode);
        if(existing.isPresent()) {
            existing.get().setEnabled(true);
        } else {
            AFeatureFlag featureFlag = featureFlagManagementService.getFeatureFlag(feature, server);
            featureModeManagementService.createMode(featureFlag, mode, true);
        }
    }

    @Override
    public void setFutureModeForFuture(FeatureEnum featureEnum, AServer server, FeatureMode mode, Boolean newValue) {
        if(newValue) {
            enableFeatureModeForFeature(featureEnum, server, mode);
        } else {
            disableFeatureModeForFeature(featureEnum, server, mode);
        }
    }

    @Override
    public void disableFeatureModeForFeature(FeatureEnum featureEnum, AServer server, FeatureMode mode) {
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        Optional<AFeatureMode> existing = featureModeManagementService.getFeatureMode(feature, server, mode);
        if(existing.isPresent()) {
            existing.get().setEnabled(false);
        } else {
            AFeatureFlag featureFlag = featureFlagManagementService.getFeatureFlag(feature, server);
            featureModeManagementService.createMode(featureFlag, mode, false);
        }
    }

    @Override
    public boolean featureModeActive(FeatureEnum featureEnum, AServer server, FeatureMode mode) {
        AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
        if(featureModeManagementService.doesFeatureModeExist(feature, server, mode)) {
            return featureModeManagementService.isFeatureModeActive(feature, server, mode);
        } else {
            return defaultFeatureModeManagement.getFeatureMode(feature, mode.getKey()).isEnabled();
        }
    }

    @Override
    public boolean featureModeActive(FeatureEnum featureEnum, Long serverId, FeatureMode mode) {
        AServer server = serverManagementService.loadServer(serverId);
        return featureModeActive(featureEnum, server, mode);
    }

    @Override
    public void validateActiveFeatureMode(Long serverId, FeatureEnum featureEnum, FeatureMode mode) {
        boolean featureModeActive = featureModeActive(featureEnum, serverId, mode);
        if(!featureModeActive) {
            throw new IncorrectFeatureModeException(featureEnum, Arrays.asList(mode));
        }
    }

    @Override
    public FeatureMode getFeatureModeForKey(String key) {
        return getAllAvailableFeatureModes().stream().filter(mode -> mode.getKey().equalsIgnoreCase(key)).findAny().orElseThrow(() -> new FeatureModeNotFoundException(key, getFeatureModesAsStrings()));
    }

    @Override
    public List<FeatureMode> getAllAvailableFeatureModes() {
        List<FeatureMode> featureModes = new ArrayList<>();
        featureConfigService.getAllFeatureConfigs().forEach(featureConfig -> featureModes.addAll(featureConfig.getAvailableModes()));
        return featureModes;
    }

    private List<String> getFeatureModesAsStrings() {
        return getAllAvailableFeatureModes().stream().map(FeatureMode::getKey).collect(Collectors.toList());
    }

    @Override
    public List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server) {
        List<DefaultFeatureMode> allDefaultModes = defaultFeatureModeManagement.getAll();
        List<AFeatureMode> allModesFromServer = featureModeManagementService.getFeatureModesOfServer(server);
        return combineFeatureModesWithDefault(server, allDefaultModes, allModesFromServer);
    }

    private List<FeatureModeDisplay> combineFeatureModesWithDefault(AServer server, List<DefaultFeatureMode> allDefaultModes, List<AFeatureMode> allModesFromServer) {
        List<FeatureModeDisplay> result = new ArrayList<>();
        List<AFeatureMode> activeModes = allModesFromServer.stream().filter(AFeatureMode::getEnabled).collect(Collectors.toList());
        List<AFeatureMode> disabledModes = allModesFromServer.stream().filter(aFeatureMode -> !aFeatureMode.getEnabled()).collect(Collectors.toList());
        List<String> usedModes = allModesFromServer.stream().map(aFeatureMode -> aFeatureMode.getFeatureMode().getMode()).collect(Collectors.toList());
        HashMap<String, FeatureConfig> featureConfigCache = new HashMap<>();
        Consumer<AFeatureMode> loadUsedValues = aFeatureMode -> {
            FeatureConfig featureConfig = getFeatureConfig(featureConfigCache, aFeatureMode.getFeatureFlag().getFeature());
            FeatureModeDisplay featureModeDisplay = FeatureModeDisplay
                    .builder()
                    .featureMode(aFeatureMode)
                    .isDefaultValue(false)
                    .featureConfig(featureConfig)
                    .build();
            result.add(featureModeDisplay);
        };
        activeModes.forEach(loadUsedValues);
        disabledModes.forEach(loadUsedValues);
        allDefaultModes.forEach(defaultFeatureMode -> {
            if(!usedModes.contains(defaultFeatureMode.getMode())) {
                FeatureConfig featureConfig = getFeatureConfig(featureConfigCache, defaultFeatureMode.getFeature());
                AFeatureFlag featureFlag = featureFlagManagementService.getFeatureFlag(defaultFeatureMode.getFeature(), server);
                AFeatureMode fakeMode = AFeatureMode.builder().server(server).enabled(defaultFeatureMode.isEnabled()).featureMode(defaultFeatureMode).featureFlag(featureFlag).build();
                FeatureModeDisplay featureModeDisplay = FeatureModeDisplay
                        .builder()
                        .featureMode(fakeMode)
                        .isDefaultValue(true)
                        .featureConfig(featureConfig)
                        .build();
                result.add(featureModeDisplay);
            }
        });
        return result;
    }

    private FeatureConfig getFeatureConfig(HashMap<String, FeatureConfig> featureConfigs, AFeature feature) {
        String featureKey = feature.getKey();
        FeatureConfig featureConfig;
        if (featureConfigs.containsKey(featureKey)) {
            featureConfig = featureConfigs.get(featureKey);
        } else {
            featureConfig = featureConfigService.getFeatureConfigForFeature(feature);
            featureConfigs.put(featureKey, featureConfig);
        }
        return featureConfig;
    }

    @Override
    public List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server, AFeature feature) {
        List<DefaultFeatureMode> allDefaultModes = defaultFeatureModeManagement.getFeatureModesForFeature(feature);
        List<AFeatureMode> allModesFromServer = featureModeManagementService.getFeatureModesOfFeatureInServer(server, feature);
        return combineFeatureModesWithDefault(server, allDefaultModes, allModesFromServer);
    }

    @Override
    public boolean necessaryFeatureModesMet(FeatureEnum featureEnum, List<FeatureMode> featureModes, Long serverId) {
        for (FeatureMode featureMode : featureModes) {
            if(featureModeActive(featureEnum, serverId, featureMode)) {
                return true;
            }
        }
        return featureModes.isEmpty();
    }

    @Override
    public boolean necessaryFeatureModesMet(FeatureAware featureAware, Long serverId) {
        return necessaryFeatureModesMet(featureAware.getFeature(), featureAware.getFeatureModeLimitations(), serverId);
    }


}
