package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.models.dto.CommandDto;

public interface CommandService {
    CommandDto createCommand(String name, String moduleName);
    Boolean doesCommandExist(String name);
    CommandDto findCommandByName(String name);
}
