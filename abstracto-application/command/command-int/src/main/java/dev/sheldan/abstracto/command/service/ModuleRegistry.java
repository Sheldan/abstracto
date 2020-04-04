package dev.sheldan.abstracto.command.service;


import dev.sheldan.abstracto.command.CommandHierarchy;
import dev.sheldan.abstracto.command.Module;

import java.util.List;

public interface ModuleRegistry {
    CommandHierarchy getDetailedModules();
    List<Module> getModules();
}
