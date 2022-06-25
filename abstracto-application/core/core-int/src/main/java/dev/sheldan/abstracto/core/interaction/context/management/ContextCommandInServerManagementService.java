package dev.sheldan.abstracto.core.interaction.context.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ContextCommand;
import dev.sheldan.abstracto.core.models.database.ContextCommandInServer;

import java.util.Optional;

public interface ContextCommandInServerManagementService {
    ContextCommandInServer createOrUpdateContextCommandInServer(ContextCommand contextCommand, AServer server, Long discordContextId);
    Optional<ContextCommandInServer> loadContextCommandInServer(ContextCommand contextCommand, AServer server);
}
