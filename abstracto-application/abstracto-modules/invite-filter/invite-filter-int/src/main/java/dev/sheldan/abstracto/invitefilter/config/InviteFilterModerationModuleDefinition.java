package dev.sheldan.abstracto.invitefilter.config;

import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class InviteFilterModerationModuleDefinition implements ModuleDefinition {

    public static final String MODERATION = "moderation";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(MODERATION).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
