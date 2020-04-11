package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;

public class ExperienceModule implements ModuleInterface {

    public static final String EXPERIENCE = "utility";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(EXPERIENCE).description("Module containing commands related to experience tracking.").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
