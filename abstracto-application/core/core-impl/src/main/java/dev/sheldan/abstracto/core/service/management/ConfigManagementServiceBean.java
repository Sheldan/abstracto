package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.AConfig;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigManagementServiceBean {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    public void setOrCreateStringValue(Long serverId, String name, String value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            createConfig(serverId, name, value);
        } else {
            config.setStringValue(value);
            configRepository.save(config);
        }
    }

    public void setOrCreateDoubleValue(Long serverId, String name, Double value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            createConfig(serverId, name, value);
        } else {
            config.setDoubleValue(value);
            configRepository.save(config);
        }
    }

    public AConfig createConfig(Long serverId, String name, String value) {
        AServer server = AServer.builder().id(serverId).build();
        AConfig config = AConfig
                .builder()
                .stringValue(value)
                .server(server)
                .name(name)
                .build();
        configRepository.save(config);
        return config;
    }

    public AConfig createConfig(Long serverId, String name, Double value) {
        AServer server = AServer.builder().id(serverId).build();
        AConfig config = AConfig
                .builder()
                .doubleValue(value)
                .server(server)
                .name(name)
                .build();
        configRepository.save(config);
        return config;
    }

    public AConfig createIfNotExists(Long serverId, String name, String value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            return this.createConfig(serverId, name, value);
        }
        return config;
    }

    public AConfig createIfNotExists(Long serverId, String name, Double value) {
        AConfig config = loadConfig(serverId, name);
        if(config == null) {
            return this.createConfig(serverId, name, value);
        }
        return config;
    }

    public AConfig loadConfig(Long serverId, String name) {
        return configRepository.findAConfigByServerIdAndName(serverId, name);
    }

}
