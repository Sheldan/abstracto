package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class SupportModuleInterface implements ModuleInterface {


    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name("support").description("Utilities for support").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
