package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface CommandInServerService {
    ACommandInAServer getCommandInAServer(Long serverId, String name);
    ACommandInAServer getCommandInAServer(AServer server, String name);
}
