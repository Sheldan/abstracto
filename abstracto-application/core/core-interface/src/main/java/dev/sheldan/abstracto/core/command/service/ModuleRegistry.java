package dev.sheldan.abstracto.core.command.service;


import dev.sheldan.abstracto.core.command.config.CommandHierarchy;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;

import java.util.List;

public interface ModuleRegistry {
    CommandHierarchy getDetailedModules();
    List<ModuleInterface> getModuleInterfaces();
}
