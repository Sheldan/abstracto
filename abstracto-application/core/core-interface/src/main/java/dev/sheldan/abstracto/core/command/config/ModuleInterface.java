package dev.sheldan.abstracto.core.command.config;


import dev.sheldan.abstracto.core.command.config.ModuleInfo;

public interface ModuleInterface {
    ModuleInfo getInfo();
    String getParentModule();
}
