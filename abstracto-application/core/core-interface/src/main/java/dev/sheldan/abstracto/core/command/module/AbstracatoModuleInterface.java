package dev.sheldan.abstracto.core.command.module;

import dev.sheldan.abstracto.core.command.ModuleInterface;
import org.springframework.stereotype.Service;

@Service
public class AbstracatoModuleInterface implements ModuleInterface {
    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name("default").description("Default module provided by abstracto").build();
    }

    @Override
    public String getParentModule() {
        return null;
    }
}
