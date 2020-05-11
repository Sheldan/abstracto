package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WarnDecayConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configManagementService;

    @Value("${abstracto.warnings.warnDecay.days}")
    private Long decayDays;

    @Override
    public void updateServerConfig(AServer server) {
        configManagementService.createIfNotExists(server.getId(), "decayDays", decayDays.longValue());
    }
}
