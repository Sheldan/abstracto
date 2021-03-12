package dev.sheldan.abstracto.repostdetection.config;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class RepostDetectionModuleDefinition implements ModuleDefinition {

    public static final String REPOST_DETECTION = "repostDetection";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(REPOST_DETECTION).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return UtilityModuleDefinition.UTILITY;
    }
}
