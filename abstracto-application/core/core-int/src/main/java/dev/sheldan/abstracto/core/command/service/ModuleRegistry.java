package dev.sheldan.abstracto.core.command.service;


import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.config.SingleLevelPackedModule;

import java.util.List;

public interface ModuleRegistry {
    List<ModuleDefinition> getModuleInterfaces();
    SingleLevelPackedModule getPackedModule(ModuleDefinition moduleDefinition);
    boolean moduleExists(String name);
    ModuleDefinition getModuleByName(String name);
    List<ModuleDefinition> getSubModules(ModuleDefinition moduleDefinition);
    ModuleDefinition getDefaultModule();
}
