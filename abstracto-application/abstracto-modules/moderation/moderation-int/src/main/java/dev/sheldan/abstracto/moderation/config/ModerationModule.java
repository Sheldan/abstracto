package dev.sheldan.abstracto.moderation.config;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class ModerationModule implements ModuleInterface {

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
