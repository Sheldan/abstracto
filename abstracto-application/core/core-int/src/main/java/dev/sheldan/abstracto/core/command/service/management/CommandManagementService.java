package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.AModule;
import dev.sheldan.abstracto.core.models.database.AFeature;

import java.util.List;
import java.util.Optional;

public interface CommandManagementService {
    ACommand createCommand(String name, String moduleName, String featureName);
    ACommand createCommand(String name, AModule moduleName, AFeature feature);
    Optional<ACommand> findCommandByNameOptional(String name);
    ACommand findCommandByName(String name);
    boolean doesCommandExist(String name);
    List<ACommand> getAllCommands();
}
