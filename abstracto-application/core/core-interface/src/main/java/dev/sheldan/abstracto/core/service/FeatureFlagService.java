package dev.sheldan.abstracto.core.service;

public interface FeatureFlagService {
    boolean isFeatureEnabled(String name, Long serverId);
    void enableFeature(String name, Long serverId);
    void disableFeature(String name, Long serverId);
}
