package dev.sheldan.abstracto.core.interaction.context.management;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.ContextCommand;
import dev.sheldan.abstracto.core.models.database.ContextType;

public interface ContextCommandManagementService {
    ContextCommand createContextCommand(String name, ContextType contextType, AFeature feature);
    ContextCommand findContextCommand(String name);
}
