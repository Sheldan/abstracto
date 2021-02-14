package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AConfig;

public interface ConfigService {
    Double getDoubleValue(String name, Long serverId);
    Long getLongValue(String name, Long serverId);
    Double getDoubleValue(String name, Long serverId, Double defaultValue);
    String getStringValue(String name, Long serverId, String defaultValue);
    Long getLongValue(String name, Long serverId, Long defaultValue);
    AConfig setOrCreateConfigValue(Long serverId, String name, AConfig value);
    void setDoubleValue(String name, Long serverId, Double value);
    void setLongValue(String name, Long serverId, Long value);
    void setConfigValue(String name, Long serverId, String value);
    AConfig setOrCreateConfigValue(String name, Long serverId, String value);
    void setConfigValue(String name, Long serverId, AConfig value);
    void setStringValue(String name, Long serverId, String value);
    boolean configurationIsValid(String name, String value);
    AConfig getFakeConfigForValue(String name, String value);

}
