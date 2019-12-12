package dev.sheldan.abstracto.command;


import java.util.List;

public interface ModuleRegistry {
    CommandHierarchy getDetailedModules();
    List<Module> getModules();
}
