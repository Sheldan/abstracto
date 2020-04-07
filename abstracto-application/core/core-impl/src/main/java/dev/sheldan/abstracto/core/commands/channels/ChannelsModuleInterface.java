package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class ChannelsModuleInterface implements ModuleInterface {

    public static final String CHANNELS = "channels";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(CHANNELS).description("Includes utilities to configure the channel configuration stored in the database").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
