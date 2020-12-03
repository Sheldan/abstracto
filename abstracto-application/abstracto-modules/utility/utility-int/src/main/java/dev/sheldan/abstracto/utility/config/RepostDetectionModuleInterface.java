package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class RepostDetectionModuleInterface implements ModuleInterface {

    public static final String REPOST_DETECTION = "repostDetection";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(REPOST_DETECTION).description("Commands related to repost detection").build();
    }

    @Override
    public String getParentModule() {
        return UtilityModuleInterface.UTILITY;
    }
}
