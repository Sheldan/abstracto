package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.AModule;

public interface ModuleManagementService {
    AModule createModule(String name);
    AModule getOrCreate(String name);
    AModule findModuleByName(String name);
    Boolean doesModuleExist(String name);
}
