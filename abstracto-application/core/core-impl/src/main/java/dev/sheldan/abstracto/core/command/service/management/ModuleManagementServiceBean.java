package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.models.AModule;
import dev.sheldan.abstracto.core.command.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModuleManagementServiceBean {

    @Autowired
    private ModuleRepository moduleRepository;

    public AModule createModule(String name) {
        AModule module = AModule.
                builder()
                .name(name)
                .build();
        moduleRepository.save(module);
        return module;
    }

    public AModule getOrCreate(String name) {
        AModule module = findModuleByName(name);
        if(module == null) {
            return createModule(name);
        }
        return module;
    }

    public AModule findModuleByName(String name) {
        return moduleRepository.findByName(name);
    }

    public Boolean doesModuleExist(String name) {
        return findModuleByName(name) != null;
    }
}
