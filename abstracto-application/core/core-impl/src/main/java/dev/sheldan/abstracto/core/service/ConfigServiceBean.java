package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.ConfigurationKeyNotFoundException;
import dev.sheldan.abstracto.core.models.database.AConfig;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigServiceBean implements ConfigService {

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Override
    public Double getDoubleValue(String name, Long serverId) {
       return getDoubleValue(name, serverId, 0D);
    }

    @Override
    public Long getLongValue(String name, Long serverId) {
        return getLongValue(name, serverId, 0L);
    }

    @Override
    public Long getLongValueOrConfigDefault(String name, Long serverId) {
        return getLongValue(name, serverId, defaultConfigManagementService.getDefaultConfig(name).getLongValue());
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
    public Double getDoubleValueOrConfigDefault(String name, Long serverId, Double defaultValue) {
        return getDoubleValue(name, serverId, defaultConfigManagementService.getDefaultConfig(name).getDoubleValue());
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
    public String getStringValueOrConfigDefault(String name, Long serverId, String defaultValue) {
        return getStringValue(name, serverId, defaultConfigManagementService.getDefaultConfig(name).getStringValue());
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
            throw new ConfigurationKeyNotFoundException(name);
        }
    }

    @Override
    public void setLongValue(String name, Long serverId, Long value) {
        if(configManagementService.configExists(serverId, name)) {
            configManagementService.setLongValue(serverId, name, value);
        } else {
            throw new ConfigurationKeyNotFoundException(name);
        }
    }

    @Override
    public void setConfigValue(String name, Long serverId, String value) {
        if(configManagementService.configExists(serverId, name)) {
            AConfig existing = configManagementService.loadConfig(serverId, name);
            if(existing.getDoubleValue() != null) {
                setDoubleValue(name, serverId, Double.parseDouble(value));
            } else if(existing.getLongValue() != null) {
                setLongValue(name, serverId, Long.parseLong(value));
            } else {
                setStringValue(name, serverId, value);
            }
        } else {
            throw new ConfigurationKeyNotFoundException(name);
        }

    }

    @Override
    public AConfig setOrCreateConfigValue(String name, Long serverId, String value) {
        if(defaultConfigManagementService.configKeyExists(name)) {
            AConfig fakeConfigValue = getFakeConfigForValue(name, value);
            return setOrCreateConfigValue(serverId, fakeConfigValue.getName(), fakeConfigValue);
        } else {
            throw new ConfigurationKeyNotFoundException(name);
        }
    }

    @Override
    public AConfig setOrCreateConfigValue(Long serverId, String name, AConfig value) {
        if(value.getDoubleValue() != null) {
            return configManagementService.setOrCreateDoubleValue(serverId, name, value.getDoubleValue());
        } else if(value.getLongValue() != null) {
            return configManagementService.setOrCreateLongValue(serverId, name, value.getLongValue());
        } else {
            return configManagementService.setOrCreateStringValue(serverId, name, value.getStringValue());
        }
    }

    @Override
    public void setConfigValue(String name, Long serverId, AConfig value) {
        if(value.getDoubleValue() != null) {
            setDoubleValue(name, serverId, value.getDoubleValue());
        } else if(value.getLongValue() != null) {
            setLongValue(name, serverId, value.getLongValue());
        } else {
            setStringValue(name, serverId, value.getStringValue());
        }
    }

    @Override
    public void setStringValue(String name, Long serverId, String value) {
        if(configManagementService.configExists(serverId, name)) {
            configManagementService.setStringValue(serverId, name, value);
        } else {
            throw new ConfigurationKeyNotFoundException(name);
        }
    }

    @Override
    public boolean configurationIsValid(String name, String value) {
        try {
            getFakeConfigForValue(name, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AConfig getFakeConfigForValue(String name, String value) {
        if(defaultConfigManagementService.configKeyExists(name)) {
            SystemConfigProperty defaultConfig = defaultConfigManagementService.getDefaultConfig(name);
            AConfig newConfig = AConfig.builder().name(defaultConfig.getName()).build();
            if(defaultConfig.getDoubleValue() != null) {
                newConfig.setDoubleValue(Double.parseDouble(value));
            } else if(defaultConfig.getLongValue() != null) {
                newConfig.setLongValue(Long.parseLong(value));
            } else {
                newConfig.setStringValue(value);
            }
            return newConfig;
        } else {
            throw new ConfigurationKeyNotFoundException(name);
        }
    }

    @Override
    public void resetConfigForKey(String configKey, Long serverId) {
        configManagementService.deleteConfig(serverId, configKey);
    }

    @Override
    public void resetConfigForFeature(String featureKey, Long serverId) {
        FeatureConfig featureConfig = featureConfigService.getFeatureDisplayForFeature(featureKey);
        featureConfig.getRequiredSystemConfigKeys().forEach(s -> {
            if(configManagementService.configExists(serverId, s)) {
                resetConfigForKey(s, serverId);
            }
        });
    }

    @Override
    public void resetConfigForServer(Long serverId) {
        configManagementService.deleteConfigForServer(serverId);
    }
}
