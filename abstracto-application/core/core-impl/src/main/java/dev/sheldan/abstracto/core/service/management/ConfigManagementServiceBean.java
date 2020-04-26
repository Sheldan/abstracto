package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigManagementServiceBean implements ConfigManagementService {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public void setOrCreateStringValue(Long serverId, String name, String value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            createConfig(serverId, name, value);
        } else {
            config.setStringValue(value);
        }
    }

    @Override
    public void setOrCreateDoubleValue(Long serverId, String name, Double value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            createConfig(serverId, name, value);
        } else {
            config.setDoubleValue(value);
        }
    }

    @Override
    public AConfig createConfig(Long serverId, String name, String value) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AConfig config = AConfig
                .builder()
                .stringValue(value)
                .server(server)
                .name(name)
                .build();
        configRepository.save(config);
        return config;
    }

    @Override
    public AConfig createConfig(Long serverId, String name, Double value) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AConfig config = AConfig
                .builder()
                .doubleValue(value)
                .server(server)
                .name(name)
                .build();
        configRepository.save(config);
        return config;
    }

    @Override
    public AConfig createIfNotExists(Long serverId, String name, String value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            return this.createConfig(serverId, name, value);
        }
        return config;
    }

    @Override
    public AConfig createIfNotExists(Long serverId, String name, Double value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            return this.createConfig(serverId, name, value);
        }
        return config;
    }

    @Override
    public AConfig loadConfig(Long serverId, String name) {
        return configRepository.findAConfigByServerIdAndName(serverId, name);
    }

    @Override
    public boolean configExists(Long serverId, String name) {
        return loadConfig(serverId, name) != null;
    }

    @Override
    public void setDoubleValue(Long serverId, String name, Double value) {
        AConfig config = loadConfig(serverId, name);
        config.setDoubleValue(value);
    }

    @Override
    public void setStringValue(Long serverId, String name, String value) {
        AConfig config = loadConfig(serverId, name);
        config.setStringValue(value);
    }

}
