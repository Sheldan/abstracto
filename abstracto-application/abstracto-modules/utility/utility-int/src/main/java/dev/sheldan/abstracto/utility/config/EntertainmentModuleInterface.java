package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class EntertainmentModuleInterface implements ModuleInterface {

    public static final String ENTERTAINMENT = "entertainment";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(ENTERTAINMENT).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
