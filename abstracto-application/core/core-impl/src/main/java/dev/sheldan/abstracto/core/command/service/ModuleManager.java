package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.config.SingleLevelPackedModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleManager implements ModuleRegistry {

    @Autowired
    private List<ModuleDefinition> moduleDefinitions;

    @Autowired
    private CommandRegistry commandRegistry;

    @Override
    public List<ModuleDefinition> getModuleInterfaces() {
        return moduleDefinitions;
    }

    @Override
    public SingleLevelPackedModule getPackedModule(ModuleDefinition moduleDefinition) {
        List<Command> commands = commandRegistry.getAllCommandsFromModule(moduleDefinition);
        return SingleLevelPackedModule
                .builder()
                .commands(commands)
                .moduleDefinition(moduleDefinition)
                .build();
    }

    @Override
    public boolean moduleExists(String name) {
        return moduleDefinitions.stream().anyMatch(moduleInterface -> moduleInterface.getInfo().getName().equalsIgnoreCase(name));
    }

    @Override
    public ModuleDefinition getModuleByName(String name) {
        return moduleDefinitions.stream().filter(moduleInterface -> moduleInterface.getInfo().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public List<ModuleDefinition> getSubModules(ModuleDefinition parentModuleDefinition) {
        return moduleDefinitions.stream().filter(moduleInterface -> moduleInterface.getParentModule() != null && moduleInterface.getParentModule().equals(parentModuleDefinition.getInfo().getName())).collect(Collectors.toList());
    }

    @Override
    public ModuleDefinition getDefaultModule() {
        return getModuleByName("default");
    }

}
