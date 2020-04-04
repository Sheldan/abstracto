package dev.sheldan.abstracto.core.service;

public interface FeatureFlagService {
    boolean isFeatureEnabled(String name, Long serverId);
}
