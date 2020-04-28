package dev.sheldan.abstracto.core.command.service;


import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.config.SingleLevelPackedModule;

import java.util.List;

public interface ModuleRegistry {
    List<ModuleInterface> getModuleInterfaces();
    SingleLevelPackedModule getPackedModule(ModuleInterface moduleInterface);
    boolean moduleExists(String name);
    ModuleInterface getModuleByName(String name);
    List<ModuleInterface> getSubModules(ModuleInterface moduleInterface);
    ModuleInterface getDefaultModule();
}
