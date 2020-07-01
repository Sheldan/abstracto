package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoreServiceConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        configManagementService.createIfNotExists(server.getId(), CommandManager.PREFIX, defaultConfigManagementService.getDefaultConfig(CommandManager.PREFIX).getStringValue());
    }
}
