package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class UtilityModuleInterface implements ModuleInterface {

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
