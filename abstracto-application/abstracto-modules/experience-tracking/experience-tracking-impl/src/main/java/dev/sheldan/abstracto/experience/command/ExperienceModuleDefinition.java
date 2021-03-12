package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class ExperienceModuleDefinition implements ModuleDefinition {

    public static final String EXPERIENCE = "experience";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(EXPERIENCE).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
