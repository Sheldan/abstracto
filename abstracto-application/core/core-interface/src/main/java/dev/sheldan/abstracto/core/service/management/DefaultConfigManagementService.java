package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ADefaultConfig;

public interface DefaultConfigManagementService {
    void createDefaultConfig(String key, String value);
    void createDefaultConfig(String key, Long value);
    void createDefaultConfig(String key, Double value);
    ADefaultConfig getDefaultConfig(String key);
}
