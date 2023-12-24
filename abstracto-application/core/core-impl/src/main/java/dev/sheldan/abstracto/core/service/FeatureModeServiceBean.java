package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.command.exception.IncorrectFeatureModeException;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.exception.FeatureModeNotFoundException;
import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.property.FeatureModeProperty;
import dev.sheldan.abstracto.core.models.template.commands.AFeatureModeDisplay;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureModeManagement;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FeatureModeServiceBean implements FeatureModeService, AsyncStartupListener {

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureModeManagementService featureModeManagementService;

    @Autowired
    private DefaultFeatureModeManagement defaultFeatureModeManagement;

    @Autowired
    private ServerManagementService serverManagementService;

    private HashMap<String, List<String>> featureModes = new HashMap<>();

    @Override
    public void enableFeatureModeForFeature(FeatureDefinition featureDefinition, AServer server, FeatureMode mode) {
        setOrCreateFeatureMode(featureDefinition, server, mode, true);
    }

    private void setOrCreateFeatureMode(FeatureDefinition featureDefinition, AServer server, FeatureMode mode, boolean featureModeState) {
        AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
        Optional<AFeatureMode> existing = featureModeManagementService.getFeatureMode(feature, server, mode);
        if (existing.isPresent()) {
            existing.get().setEnabled(featureModeState);
        } else {
            Optional<AFeatureFlag> featureFlagOptional = featureFlagManagementService.getFeatureFlag(feature, server);
            AFeatureFlag featureFlagInstance = featureFlagOptional.orElseGet(() -> featureFlagService.createInstanceFromDefaultConfig(featureDefinition, server));
            featureModeManagementService.createMode(featureFlagInstance, mode, featureModeState);
        }
    }

    @Override
    public void setFutureModeForFuture(FeatureDefinition featureDefinition, AServer server, FeatureMode mode, Boolean newValue) {
        if(newValue) {
            enableFeatureModeForFeature(featureDefinition, server, mode);
        } else {
            disableFeatureModeForFeature(featureDefinition, server, mode);
        }
    }

    @Override
    public void disableFeatureModeForFeature(FeatureDefinition featureDefinition, AServer server, FeatureMode mode) {
        setOrCreateFeatureMode(featureDefinition, server, mode, false);
    }

    @Override
    public boolean featureModeActive(FeatureDefinition featureDefinition, AServer server, FeatureMode mode) {
        AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
        if(featureModeManagementService.doesFeatureModeExist(feature, server, mode)) {
            return featureModeManagementService.isFeatureModeActive(feature, server, mode);
        } else {
            return defaultFeatureModeManagement.getFeatureMode(feature, mode.getKey()).getEnabled();
        }
    }

    @Override
    public boolean featureModeActive(FeatureDefinition featureDefinition, Long serverId, FeatureMode mode) {
        AServer server = serverManagementService.loadServer(serverId);
        return featureModeActive(featureDefinition, server, mode);
    }

    @Override
    public boolean featureModeActive(FeatureDefinition featureDefinition, Guild guild, FeatureMode mode) {
        return featureModeActive(featureDefinition, guild.getIdLong(), mode);
    }

    @Override
    public void validateActiveFeatureMode(Long serverId, FeatureDefinition featureDefinition, FeatureMode mode) {
        boolean featureModeActive = featureModeActive(featureDefinition, serverId, mode);
        if(!featureModeActive) {
            throw new IncorrectFeatureModeException(featureDefinition, Arrays.asList(mode));
        }
    }

    @Override
    public FeatureMode getFeatureModeForKey(String featureKey, String featureModeKey) {
        return getAllAvailableFeatureModes()
                .stream()
                .filter(mode -> mode.getKey().equalsIgnoreCase(featureModeKey))
                .findAny()
                .orElseThrow(() -> new FeatureModeNotFoundException(featureModeKey, getFeatureModesAsStrings(featureKey)));
    }

    @Override
    public List<FeatureMode> getAllAvailableFeatureModes() {
        List<FeatureMode> fullFeatureModes = new ArrayList<>();
        List<FeatureConfig> allFeatureConfigs = featureConfigService.getAllFeatureConfigs();
        if(allFeatureConfigs != null) {
            allFeatureConfigs.forEach(featureConfig -> fullFeatureModes.addAll(featureConfig.getAvailableModes()));
        }
        return fullFeatureModes;
    }

    private List<String> getFeatureModesAsStrings(String featureKey) {
        return featureModes.get(featureKey);
    }

    @Override
    public List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server) {
        List<FeatureModeProperty> allDefaultModes = defaultFeatureModeManagement.getAll();
        List<AFeatureMode> allModesFromServer = featureModeManagementService.getFeatureModesOfServer(server);
        return combineFeatureModesWithDefault(server, allDefaultModes, allModesFromServer);
    }

    private List<FeatureModeDisplay> combineFeatureModesWithDefault(AServer server, List<FeatureModeProperty> allDefaultModes, List<AFeatureMode> allModesFromServer) {
        List<FeatureModeDisplay> result = new ArrayList<>();
        List<AFeatureMode> activeModes = allModesFromServer.stream().filter(AFeatureMode::getEnabled).collect(Collectors.toList());
        List<AFeatureMode> disabledModes = allModesFromServer.stream().filter(aFeatureMode -> !aFeatureMode.getEnabled()).collect(Collectors.toList());
        List<String> usedModes = allModesFromServer.stream().map(AFeatureMode::getFeatureMode).collect(Collectors.toList());
        HashMap<String, FeatureConfig> featureConfigCache = new HashMap<>();
        Consumer<AFeatureMode> loadUsedValues = aFeatureMode -> {
            FeatureConfig featureConfig = getFeatureConfig(featureConfigCache, aFeatureMode.getFeatureFlag().getFeature().getKey());
            FeatureModeDisplay featureModeDisplay = FeatureModeDisplay
                    .builder()
                    .featureMode(AFeatureModeDisplay.fromFeatureMode(aFeatureMode))
                    .isDefaultValue(false)
                    .featureConfig(featureConfig)
                    .build();
            result.add(featureModeDisplay);
        };
        activeModes.forEach(loadUsedValues);
        disabledModes.forEach(loadUsedValues);
        allDefaultModes.forEach(defaultFeatureMode -> {
            if(!usedModes.contains(defaultFeatureMode.getMode())) {
                FeatureConfig featureConfig = getFeatureConfig(featureConfigCache, defaultFeatureMode.getFeatureName());
                AFeatureModeDisplay modeDisplay = AFeatureModeDisplay
                        .builder()
                        .enabled(defaultFeatureMode.getEnabled())
                        .featureMode(defaultFeatureMode.getMode())
                        .build();
                FeatureModeDisplay featureModeDisplay = FeatureModeDisplay
                        .builder()
                        .featureMode(modeDisplay)
                        .isDefaultValue(true)
                        .featureConfig(featureConfig)
                        .build();
                result.add(featureModeDisplay);
            }
        });
        return result;
    }

    private FeatureConfig getFeatureConfig(HashMap<String, FeatureConfig> featureConfigs, String featureKey) {
        FeatureConfig featureConfig;
        if (featureConfigs.containsKey(featureKey)) {
            featureConfig = featureConfigs.get(featureKey);
        } else {
            featureConfig = featureConfigService.getFeatureDisplayForFeature(featureKey);
            featureConfigs.put(featureKey, featureConfig);
        }
        return featureConfig;
    }

    @Override
    public List<FeatureModeDisplay> getEffectiveFeatureModes(AServer server, AFeature feature) {
        List<FeatureModeProperty> allDefaultModes = defaultFeatureModeManagement.getFeatureModesForFeature(feature);
        List<AFeatureMode> allModesFromServer = featureModeManagementService.getFeatureModesOfFeatureInServer(server, feature);
        return combineFeatureModesWithDefault(server, allDefaultModes, allModesFromServer);
    }

    @Override
    public boolean necessaryFeatureModesMet(FeatureDefinition featureDefinition, List<FeatureMode> featureModes, Long serverId) {
        for (FeatureMode featureMode : featureModes) {
            if(featureModeActive(featureDefinition, serverId, featureMode)) {
                return true;
            }
        }
        return featureModes.isEmpty();
    }

    @Override
    public boolean necessaryFeatureModesMet(FeatureAware featureAware, Long serverId) {
        return necessaryFeatureModesMet(featureAware.getFeature(), featureAware.getFeatureModeLimitations(), serverId);
    }


    @Override
    public void execute() {
        List<FeatureConfig> allFeatureConfigs = featureConfigService.getAllFeatureConfigs();
        log.info("Loading feature modes.");
        if(allFeatureConfigs != null) {
            allFeatureConfigs.forEach(featureConfig -> {
                List<String> modes = new ArrayList<>();
                featureConfig.getAvailableModes().forEach(featureMode -> modes.add(featureMode.getKey()));
                featureModes.put(featureConfig.getFeature().getKey(), modes);
            });
        }
    }
}
