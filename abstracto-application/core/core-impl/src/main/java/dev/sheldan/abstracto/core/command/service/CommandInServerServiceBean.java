package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandInServerServiceBean implements CommandInServerService {

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Override
    public ACommandInAServer getCommandInAServer(Long serverId, String name) {
        AServer server = serverManagementService.loadServer(serverId);
        return getCommandInAServer(server, name);
    }

    @Override
    public ACommandInAServer getCommandInAServer(AServer server, String name) {
        ACommand command = commandManagementService.findCommandByName(name);
        return commandInServerManagementService.getCommandForServer(command, server);
    }
}
