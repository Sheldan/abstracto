package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.RoleParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;

@Component
public class RoleParameterHandlerImpl implements RoleParameterHandler {
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Role.class);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        String inputString = (String) input.getValue();
        Matcher matcher = Message.MentionType.ROLE.getPattern().matcher(inputString);
        if(matcher.matches()) {
            return iterators.getRoleIterator().next();
        } else {
            if(NumberUtils.isParsable(inputString)) {
                long roleId = Long.parseLong(inputString);
                return context.getGuild().getRoleById(roleId);
            } else {
                List<Role> roles = context.getGuild().getRolesByName(inputString, true);
                if(roles.isEmpty()) {
                    throw new AbstractoTemplatedException("No role found with name.", "no_role_found_by_name_exception");
                }
                if(roles.size() > 1) {
                    throw new AbstractoTemplatedException("Multiple roles found with name.", "multiple_roles_found_by_name_exception");
                }
                return roles.get(0);
            }
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
