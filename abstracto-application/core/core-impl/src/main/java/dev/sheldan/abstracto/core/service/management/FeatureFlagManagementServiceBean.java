package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
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
    public void createFeatureFlag(AFeature feature, Long serverId, Boolean newValue) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        createFeatureFlag(feature, server, newValue);
    }

    @Override
    public void createFeatureFlag(AFeature feature, AServer server, Boolean newValue) {
        AFeatureFlag featureFlag = AFeatureFlag
                .builder()
                .enabled(newValue)
                .feature(feature)
                .server(server)
                .build();
        repository.save(featureFlag);
    }


    @Override
    public boolean getFeatureFlagValue(FeatureEnum key, Long serverId) {
        AFeature feature = featureManagementService.getFeature(key.getKey());
        Optional<AFeatureFlag> featureFlag = getFeatureFlag(feature, serverId);
        return featureFlag.isPresent() && featureFlag.get().isEnabled();
    }

    @Override
    public void updateFeatureFlag(FeatureEnum key, Long serverId, Boolean newValue) {
        AFeature feature = featureManagementService.getFeature(key.getKey());
        Optional<AFeatureFlag> existing = getFeatureFlag(feature, serverId);
        if(existing.isPresent()) {
            AFeatureFlag flag = existing.get();
            flag.setEnabled(newValue);
            repository.save(flag);
        }
    }

    @Override
    public Optional<AFeatureFlag> getFeatureFlag(AFeature feature, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return Optional.ofNullable(repository.findByServerAndFeature(server, feature));
    }

    @Override
    public List<AFeatureFlag> getFeatureFlagsOfServer(AServer server) {
        return repository.findAllByServer(server);
    }
}
