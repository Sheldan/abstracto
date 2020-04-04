package dev.sheldan.abstracto.core.command.service;


import dev.sheldan.abstracto.core.command.CommandHierarchy;
import dev.sheldan.abstracto.core.command.ModuleInterface;

import java.util.List;

public interface ModuleRegistry {
    CommandHierarchy getDetailedModules();
    List<ModuleInterface> getModuleInterfaces();
}
