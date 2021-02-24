package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.DefaultConfigProperties;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultConfigManagementServiceBean implements DefaultConfigManagementService {

    @Autowired
    private DefaultConfigProperties defaultConfigProperties;

    @Override
    public SystemConfigProperty getDefaultConfig(String key) {
        return defaultConfigProperties.getSystemConfigs().get(key.toLowerCase());
    }

    @Override
    public boolean configKeyExists(String key) {
        return defaultConfigProperties.getSystemConfigs().containsKey(key.toLowerCase());
    }
}
