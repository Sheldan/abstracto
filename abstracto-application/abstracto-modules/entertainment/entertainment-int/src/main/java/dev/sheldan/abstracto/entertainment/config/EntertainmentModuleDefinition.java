package dev.sheldan.abstracto.entertainment.config;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class EntertainmentModuleDefinition implements ModuleDefinition {

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
