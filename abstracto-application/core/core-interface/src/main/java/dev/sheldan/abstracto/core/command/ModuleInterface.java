package dev.sheldan.abstracto.core.command;


import dev.sheldan.abstracto.core.command.module.ModuleInfo;

public interface ModuleInterface {
    ModuleInfo getInfo();
    String getParentModule();
}
