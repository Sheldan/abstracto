package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;

public interface DefaultConfigManagementService {
    SystemConfigProperty getDefaultConfig(String key);
    boolean configKeyExists(String key);
}
