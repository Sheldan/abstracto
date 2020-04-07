package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class UtilityModuleInterface implements ModuleInterface {

    public static final String UTILITY = "utility";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(UTILITY).description("General utilities").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
