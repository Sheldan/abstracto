package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class SupportModuleInterface implements ModuleInterface {


    public static final String SUPPORT = "support";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(SUPPORT).description("Utilities for support").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
