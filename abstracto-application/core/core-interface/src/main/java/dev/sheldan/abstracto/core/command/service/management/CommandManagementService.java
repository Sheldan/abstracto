package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.models.database.AModule;

public interface CommandManagementService {
    ACommand createCommand(String name, String moduleName);
    ACommand createCommand(String name, AModule moduleName);
    ACommand findCommandByName(String name);
    Boolean doesCommandExist(String name);
}
