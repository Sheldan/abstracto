package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandHierarchy;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.config.PackedModule;
import dev.sheldan.abstracto.core.command.service.CommandRegistry;
import dev.sheldan.abstracto.core.command.service.ModuleRegistry;
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
    public CommandHierarchy getDetailedModules() {
        List<PackedModule> modulesWithCommands = new ArrayList<>();
        List<ModuleInterface> currentModuleInterfaces = getModuleInterfaces();
        currentModuleInterfaces.forEach(module -> {
            List<Command> commands = commandRegistry.getAllCommandsFromModule(module);
            PackedModule packed = PackedModule.builder().commands(commands).moduleInterface(module).subModules(new ArrayList<>()).build();
            modulesWithCommands.add(packed);
        });
        return getHierarchicalPacks(modulesWithCommands, currentModuleInterfaces);
    }

    private CommandHierarchy getHierarchicalPacks(List<PackedModule> modules, List<ModuleInterface> currentModuleInterfaces){
        List<PackedModule> hierarchical = modules.stream().filter(packedModule -> packedModule.getModuleInterface().getParentModule() == null).collect(Collectors.toList());

        List<PackedModule> subModules = modules.stream().filter(packedModule -> packedModule.getModuleInterface().getParentModule() != null).collect(Collectors.toList());
        subModules.forEach(module -> {
            List<ModuleInterface> path = getModulePath(module, currentModuleInterfaces);
            Collections.reverse(path);
            ModuleInterface rootModuleInterface = path.get(0);
            Optional<PackedModule> any = hierarchical.stream().filter(moduleInList -> moduleInList.getModuleInterface().getInfo().getName().equals(rootModuleInterface.getInfo().getName())).findAny();
            if(any.isPresent()){
                PackedModule currentNodeInHierarchy = any.get();
                for (int i = 1; i < path.size(); i++) {
                    Optional<PackedModule> nextInHierarchy = currentNodeInHierarchy.getSubModules().stream().filter(module1 -> module1.getModuleInterface().equals(module.getModuleInterface())).findAny();
                    if(nextInHierarchy.isPresent()){
                        currentNodeInHierarchy = nextInHierarchy.get();
                    } else {
                        currentNodeInHierarchy.getSubModules().add(module);
                        currentNodeInHierarchy = module;
                    }
                }
                if(path.size() == 1){
                    currentNodeInHierarchy.getSubModules().add(module);
                }
            }

        });
        return CommandHierarchy.builder().rootModules(hierarchical).build();
    }



    private List<ModuleInterface> getModulePath(PackedModule moduleToPathFor, List<ModuleInterface> currentModuleInterfaces){
        List<ModuleInterface> modulesBetweenRootAndThis = new ArrayList<>();
        ModuleInterface current = moduleToPathFor.getModuleInterface();
        modulesBetweenRootAndThis.add(current);
        while(current.getParentModule() != null){
            String parentModule = current.getParentModule();
            Optional<ModuleInterface> possibleModule = currentModuleInterfaces.stream().filter(module1 -> module1.getInfo().getName().equals(parentModule)).findFirst();
            if(possibleModule.isPresent()){
                ModuleInterface foundModuleInterface = possibleModule.get();
                modulesBetweenRootAndThis.add(foundModuleInterface);
                current = foundModuleInterface;
            } else {
                break;
            }

        }
        return modulesBetweenRootAndThis;
    }

    @Override
    public List<ModuleInterface> getModuleInterfaces() {
        return moduleInterfaces;
    }

}
