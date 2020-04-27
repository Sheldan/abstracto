package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoreServiceConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configManagementService;

    @Value("${abstracto.prefix}")
    private String prefix;

    @Override
    public void updateServerConfig(AServer server) {
        configManagementService.createIfNotExists(server.getId(), "prefix", prefix);
    }
}
