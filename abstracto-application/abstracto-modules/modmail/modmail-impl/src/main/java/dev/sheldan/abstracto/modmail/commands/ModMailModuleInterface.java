package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class ModMailModuleInterface implements ModuleInterface {

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
