package dev.sheldan.abstracto.customcommand.service.management;

import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;

import java.util.Optional;

public interface CustomCommandManagementService {
    Optional<CustomCommand> getCustomCommandByName(String name, Long serverId);
}
