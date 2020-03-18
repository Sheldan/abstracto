package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.command.Module;
import dev.sheldan.abstracto.command.module.ModuleInfo;
import org.springframework.stereotype.Component;

@Component
public class ChannelsModule implements Module {
    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name("channels").description("Includes utilities to configure the channel configuration stored in the database").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
