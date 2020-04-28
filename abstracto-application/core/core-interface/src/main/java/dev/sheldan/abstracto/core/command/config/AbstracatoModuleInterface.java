package dev.sheldan.abstracto.core.command.config;

import org.springframework.stereotype.Component;

@Component
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
