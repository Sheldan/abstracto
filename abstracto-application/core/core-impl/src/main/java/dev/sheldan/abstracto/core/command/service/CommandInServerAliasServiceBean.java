package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.exception.CommandAliasAlreadyExistsException;
import dev.sheldan.abstracto.core.command.exception.CommandAliasDoesNotExistsException;
import dev.sheldan.abstracto.core.command.exception.CommandAliasHidesCommandException;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerAliasManagementServiceBean;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommandInServerAliasServiceBean implements CommandInServerAliasService {

    @Autowired
    private CommandInServerAliasManagementServiceBean commandInServerAliasManagementServiceBean;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private CommandInServerService commandInServerService;

    @Autowired
    private CommandRegistry commandRegistry;

    @Override
    public ACommandInServerAlias createAliasForCommandInServer(Long serverId, String commandName, String alias) {
        AServer server = serverManagementService.loadServer(serverId);
        Optional<ACommandInServerAlias> existingAlias = commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, alias);
        if(existingAlias.isPresent()) {
            throw new CommandAliasAlreadyExistsException(existingAlias.get().getCommandInAServer().getCommandReference().getName());
        }
        Optional<Command> existingCommand = commandRegistry.getCommandByNameOptional(alias, false, serverId);
        if(existingCommand.isPresent()) {
            throw new CommandAliasHidesCommandException(existingCommand.get().getConfiguration().getName());
        }
        ACommandInAServer aCommandInAServer = commandInServerService.getCommandInAServer(server, commandName);
        return commandInServerAliasManagementServiceBean.createAliasForCommand(aCommandInAServer, alias);
    }

    @Override
    public Optional<ACommandInServerAlias> getCommandInServerAlias(Long serverId, String text) {
        AServer server = serverManagementService.loadServer(serverId);
        return commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, text);
    }

    @Override
    public void deleteCommandInServerAlias(Long serverId, String name) {
        AServer server = serverManagementService.loadServer(serverId);
        Optional<ACommandInServerAlias> existingAlias = commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, name);
        if(existingAlias.isPresent()) {
            commandInServerAliasManagementServiceBean.deleteCommandInServerAlias(existingAlias.get());
        } else {
            throw new CommandAliasDoesNotExistsException();
        }
    }

    @Override
    public List<String> getAliasesForCommand(Long serverId, String commandName) {
        AServer server = serverManagementService.loadServer(serverId);
        return commandInServerAliasManagementServiceBean.getAliasesForCommandInServer(server, commandName)
                .stream().map(alias -> alias.getAliasId().getName())
                .collect(Collectors.toList());
    }
}
