package dev.sheldan.abstracto.core.config;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// TODO runtime dependencies, features are required by commands, race condition
@Component
@Slf4j
public class FeatureListener {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent ctxStartEvt) {
        // Do nothing yet, because of a race condition between features and commands
    }
}
