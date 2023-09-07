package dev.sheldan.abstracto.customcommand.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;

import java.util.List;
import java.util.Optional;

public interface CustomCommandManagementService {
    Optional<CustomCommand> getCustomCommandByName(String name, Long serverId);
    CustomCommand createCustomCommand(String name, String content, AUserInAServer creator);
    void deleteCustomCommand(String name, AServer server);
    List<CustomCommand> getCustomCommands(AServer server);
    List<CustomCommand> getCustomCommandsStartingWith(String prefix, AServer server);
}
