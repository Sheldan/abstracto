package dev.sheldan.abstracto.core.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter @Builder
public class CommandHierarchy {
    private List<PackedModule> rootModules;

    public PackedModule getModuleWithName(String name){
        for (PackedModule module: rootModules) {
            PackedModule found = getModuleWithName(name, module);
            if(found != null){
                return found;
            }
        }
        return null;
    }

    private PackedModule getModuleWithName(String name, PackedModule module){
        if(module.getModuleInterface().getInfo().getName().equals(name)){
            return module;
        } else {
            for (PackedModule subModule: module.getSubModules()) {
                PackedModule possibleModule = getModuleWithName(name, subModule);
                if(possibleModule != null){
                    return possibleModule;
                }
            }
            return null;
        }
    }

    public Command getCommandWithName(String name) {
        for (PackedModule module: rootModules) {
            Command command = getCommandFromModule(name, module);
            if(command != null){
                return command;
            }
        }
        return null;
    }

    private Command getCommandFromModule(String name, PackedModule module){
        Command foundCommand = module.getCommands().stream().filter(command -> command.getConfiguration().getName().equals(name)).findAny().orElse(null);
        if(foundCommand == null){
            for (PackedModule subModule: module.getSubModules()) {
                Command command = getCommandFromModule(name, subModule);
                if(command != null){
                    return command;
                }
            }
            return null;
        } else {
            return foundCommand;
        }
    }
}
