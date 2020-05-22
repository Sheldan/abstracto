package dev.sheldan.abstracto.core.service;

public interface ConfigService {
    Double getDoubleValue(String name, Long serverId);
    Long getLongValue(String name, Long serverId);
    Double getDoubleValue(String name, Long serverId, Double defaultValue);
    String getStringValue(String name, Long serverId, String defaultValue);
    Long getLongValue(String name, Long serverId, Long defaultValue);
    void setDoubleValue(String name, Long serverId, Double value);
    void setLongValue(String name, Long serverId, Long value);
    void setConfigValue(String name, Long serverId, String value);
    void setStringValue(String name, Long serverId, String value);
    boolean configIsFitting(String name, Long serverId, String value);
    void validateConfig(String name, Long serverId, String value);

}
