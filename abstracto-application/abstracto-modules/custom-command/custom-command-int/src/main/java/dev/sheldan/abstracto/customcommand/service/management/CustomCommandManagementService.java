package dev.sheldan.abstracto.customcommand.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;

import java.util.List;
import java.util.Optional;

public interface CustomCommandManagementService {
    Optional<CustomCommand> getCustomCommandByName(String name, Long serverId);
    Optional<CustomCommand> getUserCustomCommandByName(String name, AUser user);
    Optional<CustomCommand> getUserCustomCommandByName(String name, Long userId);
    CustomCommand createCustomCommand(String name, String content, AUserInAServer creator);
    CustomCommand createUserCustomCommand(String name, String content, AUser user);
    void deleteCustomCommand(String name, AServer server);
    void deleteCustomCommand(String name, AUser user);
    List<CustomCommand> getCustomCommands(AServer server);
    List<CustomCommand> getUserCustomCommands(AUser aUser);
    List<CustomCommand> getCustomCommandsStartingWith(String prefix, AServer server);
    List<CustomCommand> getUserCustomCommandsStartingWith(String prefix, AUser aUser);
}
