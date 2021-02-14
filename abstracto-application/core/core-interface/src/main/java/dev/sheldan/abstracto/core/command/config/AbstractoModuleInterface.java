package dev.sheldan.abstracto.core.command.config;

import org.springframework.stereotype.Component;

@Component
public class AbstractoModuleInterface implements ModuleInterface {
    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name("default").templated(true).build();
    }

    @Override
    public String getParentModule() {
        return null;
    }
}
