package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.ARoleParameterHandler;
import dev.sheldan.abstracto.core.command.handler.provided.RoleParameterHandler;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ARoleParameterHandlerImpl implements ARoleParameterHandler {

    @Autowired
    private RoleParameterHandler roleParameterHandler;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CommandService commandService;

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(ARole.class);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        Parameter cloned = commandService.cloneParameter(param);
        cloned.setType(Role.class);
        Role role = (Role) roleParameterHandler.handle(input, iterators, cloned, context, command);
        return roleService.getFakeRoleFromRole(role);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
