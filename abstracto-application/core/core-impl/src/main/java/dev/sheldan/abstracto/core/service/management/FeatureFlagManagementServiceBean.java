package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FeatureFlagManagementServiceBean implements FeatureFlagManagementService {

    @Autowired
    private FeatureFlagRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public void createFeatureFlag(String key, Long serverId, Boolean newValue) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        createFeatureFlag(key, server, newValue);
    }

    @Override
    public void createFeatureFlag(String key, AServer server, Boolean newValue) {
        AFeatureFlag featureFlag = AFeatureFlag
                .builder()
                .enabled(newValue)
                .key(key)
                .server(server)
                .build();
        repository.save(featureFlag);
    }

    @Override
    public boolean getFeatureFlagValue(String key, Long serverId) {
        Optional<AFeatureFlag> featureFlag = getFeatureFlag(key, serverId);
        return featureFlag.isPresent() && featureFlag.get().isEnabled();
    }

    @Override
    public void updateFeatureFlag(String key, Long serverId, Boolean newValue) {
        Optional<AFeatureFlag> existing = getFeatureFlag(key, serverId);
        if(existing.isPresent()) {
            AFeatureFlag flag = existing.get();
            flag.setEnabled(newValue);
            repository.save(flag);
        }
    }

    @Override
    public Optional<AFeatureFlag> getFeatureFlag(String key, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return Optional.ofNullable(repository.findByServerAndKey(server, key));
    }
}
