package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.config.SingleLevelPackedModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModuleManager implements ModuleRegistry {

    @Autowired
    private List<ModuleInterface> moduleInterfaces;

    @Autowired
    private CommandRegistry commandRegistry;

    @Override
    public List<ModuleInterface> getModuleInterfaces() {
        return moduleInterfaces;
    }

    @Override
    public SingleLevelPackedModule getPackedModule(ModuleInterface moduleInterface) {
        List<Command> commands = commandRegistry.getAllCommandsFromModule(moduleInterface);
        return SingleLevelPackedModule
                .builder()
                .commands(commands)
                .moduleInterface(moduleInterface)
                .build();
    }

    @Override
    public boolean moduleExists(String name) {
        return moduleInterfaces.stream().anyMatch(moduleInterface -> moduleInterface.getInfo().getName().equalsIgnoreCase(name));
    }

    @Override
    public ModuleInterface getModuleByName(String name) {
        return moduleInterfaces.stream().filter(moduleInterface -> moduleInterface.getInfo().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public List<ModuleInterface> getSubModules(ModuleInterface parentModuleInterface) {
        return moduleInterfaces.stream().filter(moduleInterface -> moduleInterface.getParentModule() != null && moduleInterface.getParentModule().equals(parentModuleInterface.getInfo().getName())).collect(Collectors.toList());
    }

    @Override
    public ModuleInterface getDefaultModule() {
        return getModuleByName("default");
    }

}
