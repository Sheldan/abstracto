package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class ChannelsModuleInterface implements ModuleInterface {

    public static final String CHANNELS = "channels";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(CHANNELS).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
