package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.AModule;
import dev.sheldan.abstracto.core.models.database.AFeature;

public interface CommandManagementService {
    ACommand createCommand(String name, String moduleName, String featureName);
    ACommand createCommand(String name, AModule moduleName, AFeature feature);
    ACommand findCommandByName(String name);
    Boolean doesCommandExist(String name);
}
