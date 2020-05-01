package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
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
        repository.save(featureFlag);
        return featureFlag;
    }


    @Override
    public AFeatureFlag getFeatureFlag(AFeature feature, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return getFeatureFlag(feature, server);
    }

    @Override
    public AFeatureFlag getFeatureFlag(AFeature feature, AServer server) {
        return repository.findByServerAndFeature(server, feature);
    }

    @Override
    public List<AFeatureFlag> getFeatureFlagsOfServer(AServer server) {
        return repository.findAllByServer(server);
    }

    @Override
    public AFeatureFlag setFeatureFlagValue(AFeature feature, Long serverId, Boolean newValue) {
        AFeatureFlag featureFlag = getFeatureFlag(feature, serverId);
        featureFlag.setEnabled(newValue);
        return featureFlag;
    }

    @Override
    public AFeatureFlag setFeatureFlagValue(AFeature feature, AServer server, Boolean newValue) {
        AFeatureFlag featureFlag = getFeatureFlag(feature, server);
        featureFlag.setEnabled(newValue);
        return featureFlag;
    }
}
