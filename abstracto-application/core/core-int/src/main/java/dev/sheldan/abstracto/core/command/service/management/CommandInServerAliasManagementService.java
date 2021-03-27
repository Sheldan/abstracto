package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;
import java.util.Optional;

public interface CommandInServerAliasManagementService {

    List<ACommandInServerAlias> getAliasesInServer(AServer server);
    boolean doesCommandInServerAliasExist(AServer server, String alias);
    Optional<ACommandInServerAlias> getCommandInServerAlias(AServer server, String alias);
    ACommandInServerAlias createAliasForCommand(ACommandInAServer commandInAServer, String alias);
    void deleteCommandInServerAlias(ACommandInServerAlias alias);
    List<ACommandInServerAlias> getAliasesForCommandInServer(AServer server, String commandName);
}
