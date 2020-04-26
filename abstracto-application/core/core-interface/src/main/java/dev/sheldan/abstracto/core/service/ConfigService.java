package dev.sheldan.abstracto.core.service;

public interface ConfigService {
    Double getDoubleValue(String name, Long serverId);
    Double getDoubleValue(String name, Long serverId, Double defaultValue);
    String getStringValue(String name, Long serverId, String defaultValue);
    void createDoubleValueIfNotExist(String name, Long serverId, Double value);
    void setDoubleValue(String name, Long serverId, Double value);
    void setStringValue(String name, Long serverId, String value);

}
