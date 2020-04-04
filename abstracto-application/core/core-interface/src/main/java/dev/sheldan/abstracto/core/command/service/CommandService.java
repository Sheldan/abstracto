package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.models.ACommand;

public interface CommandService {
    ACommand createCommand(String name, String moduleName);
    Boolean doesCommandExist(String name);
}
