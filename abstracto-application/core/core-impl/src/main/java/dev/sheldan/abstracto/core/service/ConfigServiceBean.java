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
    public Long getLongValue(String name, Long serverId) {
        return getLongValue(name, serverId, 0L);
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
    public String getStringValue(String name, Long serverId, String defaultValue) {
        AConfig config = configManagementService.loadConfig(serverId, name);
        if(config == null) {
            return defaultValue;
        }
        return config.getStringValue();
    }

    @Override
    public Long getLongValue(String name, Long serverId, Long defaultValue) {
        AConfig config = configManagementService.loadConfig(serverId, name);
        if(config == null) {
            return defaultValue;
        }
        return config.getLongValue();
    }

    @Override
    public void setDoubleValue(String name, Long serverId, Double value) {
        if(configManagementService.configExists(serverId, name)) {
            configManagementService.setDoubleValue(serverId, name, value);
        } else {
            throw new ConfigurationException(String.format("Key %s does not exist.", name));
        }
    }

    @Override
    public void setLongValue(String name, Long serverId, Long value) {
        if(configManagementService.configExists(serverId, name)) {
            configManagementService.setLongValue(serverId, name, value);
        } else {
            throw new ConfigurationException(String.format("Key %s does not exist.", name));
        }
    }

    @Override
    public void setStringValue(String name, Long serverId, String value) {
        if(configManagementService.configExists(serverId, name)) {
            configManagementService.setStringValue(serverId, name, value);
        } else {
            throw new ConfigurationException(String.format("Key %s does not exist.", name));
        }
    }
}
