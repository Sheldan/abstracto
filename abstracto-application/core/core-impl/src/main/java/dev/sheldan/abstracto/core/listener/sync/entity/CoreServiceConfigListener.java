package dev.sheldan.abstracto.core.listener.sync.entity;

import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CoreServiceConfigListener implements ServerConfigListener {

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Creating prefix config for server {}.", server.getId());
        String defaultPrefix = defaultConfigManagementService.getDefaultConfig(CommandManager.PREFIX).getStringValue();
        configManagementService.createIfNotExists(server.getId(), CommandManager.PREFIX, defaultPrefix);
    }
}
