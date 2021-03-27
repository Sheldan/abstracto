package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.command.model.database.CommandInServerAliasId;
import dev.sheldan.abstracto.core.command.repository.CommandInServerAliasRepository;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommandInServerAliasManagementServiceBean implements CommandInServerAliasManagementService {

    @Autowired
    private CommandInServerAliasRepository repository;

    @Override
    public List<ACommandInServerAlias> getAliasesInServer(AServer server) {
        return repository.findByCommandInAServer_ServerReference(server);
    }

    @Override
    public boolean doesCommandInServerAliasExist(AServer server, String alias) {
        return repository.existsByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(server, alias);
    }

    @Override
    public Optional<ACommandInServerAlias> getCommandInServerAlias(AServer server, String alias) {
        return repository.findByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(server, alias);
    }

    @Override
    public ACommandInServerAlias createAliasForCommand(ACommandInAServer commandInAServer, String aliasName) {
        CommandInServerAliasId identifier = CommandInServerAliasId
                .builder()
                .commandInServerId(commandInAServer.getCommandInServerId())
                .name(aliasName)
                .build();
        ACommandInServerAlias alias = ACommandInServerAlias
                .builder()
                .commandInAServer(commandInAServer)
                .aliasId(identifier)
                .build();
        return repository.save(alias);
    }

    @Override
    public void deleteCommandInServerAlias(ACommandInServerAlias alias) {
        repository.delete(alias);
    }

    @Override
    public List<ACommandInServerAlias> getAliasesForCommandInServer(AServer server, String commandName) {
        return repository.findByCommandInAServer_ServerReferenceAndCommandInAServer_CommandReference_NameEqualsIgnoreCase(server, commandName);
    }
}
