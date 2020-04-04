package dev.sheldan.abstracto.command.service.management;

import dev.sheldan.abstracto.command.models.AModule;

public interface ModuleManagementService {
    AModule createModule(String name);
    AModule getOrCreate(String name);
    AModule findModuleByName(String name);
    Boolean doesModuleExist(String name);
}
