package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.FeatureFlagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class FeatureFlagManagementServiceBean implements FeatureFlagManagementService {

    @Autowired
    private FeatureFlagRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Override
    public AFeatureFlag createFeatureFlag(AFeature feature, Long serverId, Boolean newValue) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return createFeatureFlag(feature, server, newValue);
    }

    @Override
    public AFeatureFlag createFeatureFlag(AFeature feature, AServer server, Boolean newValue) {
        AFeatureFlag featureFlag = AFeatureFlag
                .builder()
                .enabled(newValue)
                .feature(feature)
                .server(server)
                .build();
        log.info("Creating new feature flag for feature {} in server {} with value {}.", feature.getKey(), server.getId(), newValue);
        repository.save(featureFlag);
        return featureFlag;
    }


    @Override
    public Optional<AFeatureFlag>  getFeatureFlag(AFeature feature, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return getFeatureFlag(feature, server);
    }

    @Override
    public Optional<AFeatureFlag>  getFeatureFlag(String featureKey, Long serverId) {
        AFeature feature = featureManagementService.getFeature(featureKey);
        return getFeatureFlag(feature, serverId);
    }

    @Override
    public Optional<AFeatureFlag>  getFeatureFlag(String featureKey, AServer server) {
        AFeature feature = featureManagementService.getFeature(featureKey);
        return getFeatureFlag(feature, server);
    }

    @Override
    public boolean featureFlagExists(AFeature feature, AServer server) {
        return repository.existsByServerAndFeature(server, feature);
    }

    @Override
    public Optional<AFeatureFlag> getFeatureFlag(AFeature feature, AServer server) {
        return repository.findByServerAndFeature(server, feature);
    }

    @Override
    public List<AFeatureFlag> getFeatureFlagsOfServer(AServer server) {
        return repository.findAllByServer(server);
    }

    @Override
    public AFeatureFlag setFeatureFlagValue(AFeature feature, Long serverId, Boolean newValue) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return setFeatureFlagValue(feature, server, newValue);
    }

    @Override
    public AFeatureFlag setFeatureFlagValue(AFeature feature, AServer server, Boolean newValue) {
        Optional<AFeatureFlag> featureFlagOptional = getFeatureFlag(feature, server);
        if(!featureFlagOptional.isPresent()) {
            log.info("Setting feature flag for feature {} in server {} to value {}.", feature.getKey(), server.getId(), newValue);
            return createFeatureFlag(feature, server, newValue);
        }
        else {
            AFeatureFlag featureFlag = featureFlagOptional.get();
            featureFlag.setEnabled(newValue);
            return featureFlag;
        }
    }
}
