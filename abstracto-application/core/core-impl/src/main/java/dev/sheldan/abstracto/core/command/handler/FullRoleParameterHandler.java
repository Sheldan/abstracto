package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FullRoleParameterHandler implements CommandParameterHandler {

    @Autowired
    private RoleParameterHandler roleParameterHandler;

    @Autowired
    private RoleService roleService;

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(FullRole.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        Role role = (Role) roleParameterHandler.handle(input, iterators, Role.class, context);
        ARole aRole = roleService.getFakeRoleFromRole(role);
        return FullRole.builder().role(aRole).serverRole(role).build();
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
