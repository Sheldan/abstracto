package dev.sheldan.abstracto.command.utility;

import dev.sheldan.abstracto.command.Module;
import dev.sheldan.abstracto.command.module.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class UtilityModule implements Module {

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name("utility").description("General utilities").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
