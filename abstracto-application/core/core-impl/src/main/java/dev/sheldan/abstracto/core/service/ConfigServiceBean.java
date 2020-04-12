package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ConfigurationException;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.models.database.AConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigServiceBean implements ConfigService{

    @Autowired
    private ConfigManagementService configManagementService;

    @Override
    public Double getDoubleValue(String name, Long serverId) {
       return getDoubleValue(name, serverId, 0D);
    }

    @Override
    public Double getDoubleValue(String name, Long serverId, Double defaultValue) {
        AConfig config = configManagementService.loadConfig(serverId, name);
        if(config == null) {
            return defaultValue;
        }
        return config.getDoubleValue();
    }

    @Override
    public void createDoubleValueIfNotExist(String name, Long serverId, Double value) {
        configManagementService.createIfNotExists(serverId, name, value);
    }

    @Override
    public void setDoubleValue(String name, Long serverId, Double value) {
        if(configManagementService.configExists(serverId, name)) {
            configManagementService.setDoubleValue(serverId, name, value);
        } else {
            throw new ConfigurationException(String.format("Key %s does not exist.", name));
        }
    }
}
