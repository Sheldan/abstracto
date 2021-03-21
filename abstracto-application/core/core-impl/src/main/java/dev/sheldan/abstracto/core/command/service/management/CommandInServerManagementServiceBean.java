package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.repository.CommandInServerRepository;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandInServerManagementServiceBean implements CommandInServerManagementService {

    @Autowired
    private CommandInServerRepository repository;


    @Override
    public ACommandInAServer crateCommandInServer(ACommand command, AServer server) {
        ACommandInAServer commandInAServer = ACommandInAServer
                .builder()
                .commandReference(command)
                .serverReference(server)
                .restricted(false)
                .build();
        log.info("Creating command {} in server {}.", command.getName(), server.getId());
        return repository.save(commandInAServer);
    }

    @Override
    public boolean doesCommandExistInServer(ACommand command, AServer server) {
        return getCommandForServer(command, server) != null;
    }

    @Override
    public ACommandInAServer getCommandForServer(ACommand command, AServer server) {
        return repository.findByServerReferenceAndCommandReference(server, command);
    }

    @Override
    public ACommandInAServer getCommandForServer(ACommand command, Long serverId) {
        return repository.findByServerReference_IdAndCommandReference(serverId, command);
    }
}
