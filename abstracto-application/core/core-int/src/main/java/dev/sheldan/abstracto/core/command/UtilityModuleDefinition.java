package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class UtilityModuleDefinition implements ModuleDefinition {

    public static final String UTILITY = "utility";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(UTILITY).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
