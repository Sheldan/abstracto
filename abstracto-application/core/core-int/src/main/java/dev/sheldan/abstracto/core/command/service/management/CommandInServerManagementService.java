package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;

public interface CommandInServerManagementService {
    ACommandInAServer createCommandInServer(ACommand command, AServer server);
    ACommandInAServer createCommandInServer(ACommand command, AServer server, Long commandId);
    boolean doesCommandExistInServer(ACommand command, AServer server);
    ACommandInAServer getCommandForServer(ACommand command, AServer server);
    ACommandInAServer getCommandForServer(ACommand command, Long serverId);
    ACommandInAServer getCommandForServer(Long commandInServerId);
    List<ACommandInAServer> getCommandsForServer(List<Long> commandInServerId);
}
