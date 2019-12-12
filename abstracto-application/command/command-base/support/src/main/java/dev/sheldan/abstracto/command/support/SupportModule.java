package dev.sheldan.abstracto.command.support;

import dev.sheldan.abstracto.command.Module;
import dev.sheldan.abstracto.command.module.AbstracatoModule;
import dev.sheldan.abstracto.command.module.ModuleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupportModule implements Module {


    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name("support").description("Utilities for support").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
