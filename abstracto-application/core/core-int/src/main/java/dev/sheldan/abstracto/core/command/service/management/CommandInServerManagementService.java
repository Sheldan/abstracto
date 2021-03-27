package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface CommandInServerManagementService {
    ACommandInAServer createCommandInServer(ACommand command, AServer server);
    boolean doesCommandExistInServer(ACommand command, AServer server);
    ACommandInAServer getCommandForServer(ACommand command, AServer server);
    ACommandInAServer getCommandForServer(ACommand command, Long serverId);
}
