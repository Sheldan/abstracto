package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

@Component
public class AssignableRoleModuleDefinition implements ModuleDefinition {
    public static final String ASSIGNABLE_ROLES = "assignableRole";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(ASSIGNABLE_ROLES).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
