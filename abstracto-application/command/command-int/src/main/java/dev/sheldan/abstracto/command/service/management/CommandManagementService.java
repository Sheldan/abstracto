package dev.sheldan.abstracto.command.service.management;

import dev.sheldan.abstracto.command.models.ACommand;
import dev.sheldan.abstracto.command.models.AModule;

public interface CommandManagementService {
    ACommand createCommand(String name, String moduleName);
    ACommand createCommand(String name, AModule moduleName);
    ACommand findCommandByName(String name);
    Boolean doesCommandExist(String name);
}
