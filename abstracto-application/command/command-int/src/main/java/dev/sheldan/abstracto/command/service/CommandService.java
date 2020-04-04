package dev.sheldan.abstracto.command.service;

import dev.sheldan.abstracto.command.models.ACommand;

public interface CommandService {
    ACommand createCommand(String name, String moduleName);
    Boolean doesCommandExist(String name);
}
