package dev.sheldan.abstracto.moderation;

import dev.sheldan.abstracto.command.Module;
import dev.sheldan.abstracto.command.module.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class Moderation implements Module {

    public static final String MODERATION = "moderation";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(MODERATION).description("Utilities to moderate the server").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
