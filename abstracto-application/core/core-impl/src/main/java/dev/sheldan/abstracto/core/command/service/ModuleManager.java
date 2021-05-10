package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.config.SingleLevelPackedModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        // some modules might be duplicated and only matched by name, this way we remove the duplicates
        Set<String> moduleNames = new HashSet<>();
        return moduleDefinitions
                .stream()
                .filter(moduleInterface -> moduleInterface.getParentModule() != null && moduleInterface.getParentModule().equals(parentModuleDefinition.getInfo().getName()))
                .filter(moduleDefinition -> moduleNames.add(moduleDefinition.getInfo().getName()))
                .collect(Collectors.toList());
    }

    @Override
    public ModuleDefinition getDefaultModule() {
        return getModuleByName("default");
    }

}
