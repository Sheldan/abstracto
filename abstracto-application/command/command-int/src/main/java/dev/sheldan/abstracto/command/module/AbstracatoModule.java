package dev.sheldan.abstracto.command.module;

import dev.sheldan.abstracto.command.Module;
import org.springframework.stereotype.Service;

@Service
public class AbstracatoModule implements Module {
    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name("default").description("Default module provided by abstracto").build();
    }

    @Override
    public String getParentModule() {
        return null;
    }
}
