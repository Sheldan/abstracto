package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.entity.AsyncServerCreatedListener;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.listener.ServerCreatedListenerModel;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CommandConfigListenerAsync implements AsyncServerCreatedListener {

    @Autowired
    private List<Command> commandList;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public DefaultListenerResult execute(ServerCreatedListenerModel model) {
        AServer server = serverManagementService.loadServer(model.getServerId());
        log.info("Creating command instances for server {}.", model.getServerId());
        commandList.forEach(command -> {
            if(command.getConfiguration() != null) {
                ACommand aCommand = commandManagementService.findCommandByName(command.getConfiguration().getName());
                if(!commandInServerManagementService.doesCommandExistInServer(aCommand, server)) {
                    commandInServerManagementService.crateCommandInServer(aCommand, server);
                }
            }
        });
        return DefaultListenerResult.PROCESSED;
    }
}
