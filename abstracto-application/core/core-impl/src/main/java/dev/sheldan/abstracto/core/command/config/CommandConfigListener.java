package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.listener.ServerConfigListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CommandConfigListener implements ServerConfigListener {

    @Autowired
    private List<Command> commandList;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Override
    public void updateServerConfig(AServer server) {
        log.info("Creating command instances for server {}.", server.getId());
        commandList.forEach(command -> {
            if(command.getConfiguration() != null) {
                ACommand aCommand = commandManagementService.findCommandByName(command.getConfiguration().getName());
                if(!commandInServerManagementService.doesCommandExistInServer(aCommand, server)) {
                    commandInServerManagementService.crateCommandInServer(aCommand, server);
                }
            }
        });
    }
}
