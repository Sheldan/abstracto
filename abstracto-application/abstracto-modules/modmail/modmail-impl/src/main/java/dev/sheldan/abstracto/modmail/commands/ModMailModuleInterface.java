package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class ModMailModuleInterface implements ModuleInterface {

    public static final String MODMAIL = "modMail";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(MODMAIL).description("Commands to be used for modmail.").build();
    }


    @Override
    public String getParentModule() {
        return "default";
    }
}
