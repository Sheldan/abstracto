package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class ExperienceModule implements ModuleInterface {

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
