package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class ConfigModuleDefinition implements ModuleDefinition {

    public static final String CONFIG = "config";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(CONFIG).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
