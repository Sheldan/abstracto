package dev.sheldan.abstracto.commands;

import dev.sheldan.abstracto.command.*;
import dev.sheldan.abstracto.command.Module;
import dev.sheldan.abstracto.command.meta.CommandRegistry;
import dev.sheldan.abstracto.command.service.ModuleRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModuleManager implements ModuleRegistry {

    @Autowired
    private List<Module> modules;

    @Autowired
    private CommandRegistry commandRegistry;

    @Override
    public CommandHierarchy getDetailedModules() {
        List<PackedModule> modulesWithCommands = new ArrayList<>();
        List<Module> currentModules = getModules();
        currentModules.forEach(module -> {
            List<Command> commands = commandRegistry.getAllCommandsFromModule(module);
            PackedModule packed = PackedModule.builder().commands(commands).module(module).subModules(new ArrayList<>()).build();
            modulesWithCommands.add(packed);
        });
        return getHierarchicalPacks(modulesWithCommands, currentModules);
    }

    private CommandHierarchy getHierarchicalPacks(List<PackedModule> modules, List<Module> currentModules){
        List<PackedModule> hierarchical = modules.stream().filter(packedModule -> packedModule.getModule().getParentModule() == null).collect(Collectors.toList());

        List<PackedModule> subModules = modules.stream().filter(packedModule -> packedModule.getModule().getParentModule() != null).collect(Collectors.toList());
        subModules.forEach(module -> {
            List<Module> path = getModulePath(module, currentModules);
            Collections.reverse(path);
            Module rootModule = path.get(0);
            Optional<PackedModule> any = hierarchical.stream().filter(moduleInList -> moduleInList.getModule().getInfo().getName().equals(rootModule.getInfo().getName())).findAny();
            if(any.isPresent()){
                PackedModule currentNodeInHierarchy = any.get();
                for (int i = 1; i < path.size(); i++) {
                    Optional<PackedModule> nextInHierarchy = currentNodeInHierarchy.getSubModules().stream().filter(module1 -> module1.getModule().equals(module.getModule())).findAny();
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



    private List<Module> getModulePath(PackedModule moduleToPathFor, List<Module> currentModules){
        List<Module> modulesBetweenRootAndThis = new ArrayList<>();
        Module current = moduleToPathFor.getModule();
        modulesBetweenRootAndThis.add(current);
        while(current.getParentModule() != null){
            String parentModule = current.getParentModule();
            Optional<Module> possibleModule = currentModules.stream().filter(module1 -> module1.getInfo().getName().equals(parentModule)).findFirst();
            if(possibleModule.isPresent()){
                Module foundModule = possibleModule.get();
                modulesBetweenRootAndThis.add(foundModule);
                current = foundModule;
            } else {
                break;
            }

        }
        return modulesBetweenRootAndThis;
    }

    @Override
    public List<Module> getModules() {
        return modules;
    }

}
