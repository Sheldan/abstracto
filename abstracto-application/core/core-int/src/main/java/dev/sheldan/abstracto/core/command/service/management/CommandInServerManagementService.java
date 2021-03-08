package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.ACommandInAServer;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface CommandInServerManagementService {
    ACommandInAServer crateCommandInServer(ACommand command, AServer server);
    boolean doesCommandExistInServer(ACommand command, AServer server);
    ACommandInAServer getCommandForServer(ACommand command, AServer server);
    ACommandInAServer getCommandForServer(ACommand command, Long serverId);
}
