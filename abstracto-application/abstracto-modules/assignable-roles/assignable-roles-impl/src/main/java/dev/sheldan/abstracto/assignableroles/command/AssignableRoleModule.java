package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import org.springframework.stereotype.Component;

@Component
public class AssignableRoleModule implements ModuleInterface {
    public static final String ASSIGNABLE_ROLES = "assignableRole";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(ASSIGNABLE_ROLES).description("Module containing commands for creating and maintaining assignable role posts.").build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
