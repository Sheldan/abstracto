package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class ModMailModuleDefinition implements ModuleDefinition {

    public static final String MODMAIL = "modmail";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(MODMAIL).templated(true).build();
    }


    @Override
    public String getParentModule() {
        return "default";
    }
}
