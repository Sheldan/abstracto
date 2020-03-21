package dev.sheldan.abstracto.utility;

import dev.sheldan.abstracto.command.Module;
import dev.sheldan.abstracto.command.module.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class Utility implements Module {

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
