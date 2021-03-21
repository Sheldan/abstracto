package dev.sheldan.abstracto.core.commands.help;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class SupportModuleDefinition implements ModuleDefinition {

    public static final String SUPPORT = "support";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(SUPPORT).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
