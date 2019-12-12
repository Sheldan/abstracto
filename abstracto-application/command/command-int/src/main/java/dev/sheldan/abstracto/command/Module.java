package dev.sheldan.abstracto.command;

import dev.sheldan.abstracto.command.module.ModuleInfo;

public interface Module {
    ModuleInfo getInfo();
    String getParentModule();
}
