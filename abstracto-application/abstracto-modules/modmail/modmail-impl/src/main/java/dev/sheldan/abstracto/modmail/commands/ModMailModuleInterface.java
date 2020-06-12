package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModMailModuleInterface implements ModuleInterface {

    public static final String MODMAIL = "modMail";

    @Autowired
    private TemplateService templateService;

    @Override
    public ModuleInfo getInfo() {
        String description = templateService.renderSimpleTemplate("modmail_help_module_info");
        return ModuleInfo.builder().name(MODMAIL).description(description).build();
    }


    @Override
    public String getParentModule() {
        return "default";
    }
}
