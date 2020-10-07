package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.database.AModule;
import dev.sheldan.abstracto.core.command.repository.ModuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModuleManagementServiceBean implements ModuleManagementService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Override
    public AModule createModule(String name) {
        AModule module = AModule.
                builder()
                .name(name)
                .build();
        moduleRepository.save(module);
        log.info("Creating module {}.", name);
        return module;
    }

    @Override
    public AModule getOrCreate(String name) {
        AModule module = findModuleByName(name);
        if(module == null) {
            return createModule(name);
        }
        return module;
    }

    @Override
    public AModule findModuleByName(String name) {
        return moduleRepository.findByName(name);
    }

    @Override
    public Boolean doesModuleExist(String name) {
        return findModuleByName(name) != null;
    }
}
