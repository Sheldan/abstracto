package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.AFeatureFlag;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.repository.FeatureFlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FeatureFlagManagementServiceBean {

    @Autowired
    private FeatureFlagRepository repository;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    public void createFeatureFlag(String key, Long serverId, Boolean newValue) {
        ServerDto server = ServerDto.builder().id(serverId).build();
        createFeatureFlag(key, server, newValue);
    }

    public void createFeatureFlag(String key, ServerDto server, Boolean newValue) {
        AServer aServer = AServer.builder().id(server.getId()).build();
        AFeatureFlag featureFlag = AFeatureFlag
                .builder()
                .enabled(newValue)
                .key(key)
                .server(aServer)
                .build();
        repository.save(featureFlag);
    }

    public boolean getFeatureFlagValue(String key, Long serverId) {
        Optional<AFeatureFlag> featureFlag = getFeatureFlag(key, serverId);
        return featureFlag.isPresent() && featureFlag.get().isEnabled();
    }

    public void updateOrCreateFeatureFlag(String key, Long serverId, Boolean newValue) {
        Optional<AFeatureFlag> existing = getFeatureFlag(key, serverId);
        if(existing.isPresent()) {
            AFeatureFlag flag = existing.get();
            flag.setEnabled(newValue);
            repository.save(flag);
        } else {
            createFeatureFlag(key, serverId, newValue);
        }
    }

    public Optional<AFeatureFlag> getFeatureFlag(String key, Long serverId) {
        AServer server = AServer.builder().id(serverId).build();
        return Optional.ofNullable(repository.findByServerAndKey(server, key));
    }
}
