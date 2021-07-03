package dev.sheldan.abstracto.core.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ComponentServiceBean implements ComponentService {
    @Override
    public String generateComponentId(Long serverId) {
        return generateComponentId();
    }

    @Override
    public String generateComponentId() {
        return UUID.randomUUID().toString();
    }
}
