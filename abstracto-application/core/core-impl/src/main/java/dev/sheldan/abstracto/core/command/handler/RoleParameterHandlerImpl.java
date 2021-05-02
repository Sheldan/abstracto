package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
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
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(Role.class) && value.getType().equals(ParameterPieceType.STRING);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        String inputString = ((String) input.getValue()).trim();
        Matcher matcher = Message.MentionType.ROLE.getPattern().matcher(inputString);
        Role foundRole;
        if(matcher.matches() && iterators.getRoleIterator().hasNext()) {
            foundRole = iterators.getRoleIterator().next();
        } else {
            if(NumberUtils.isParsable(inputString)) {
                long roleId = Long.parseLong(inputString);
                foundRole = context.getGuild().getRoleById(roleId);
            } else {
                List<Role> roles = context.getGuild().getRolesByName(inputString, true);
                if(roles.isEmpty()) {
                    throw new AbstractoTemplatedException("No role found with name.", "no_role_found_by_name_exception");
                }
                if(roles.size() > 1) {
                    throw new AbstractoTemplatedException("Multiple roles found with name.", "multiple_roles_found_by_name_exception");
                }
                foundRole = roles.get(0);
            }
        }
        if(foundRole != null && foundRole.isPublicRole()) {
            throw new AbstractoTemplatedException("Public role cannot be used for role parameter.", "everyone_role_not_allowed_exception");
        }
        return foundRole;
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
