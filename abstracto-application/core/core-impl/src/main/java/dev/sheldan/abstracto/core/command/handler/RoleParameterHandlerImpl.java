package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.RoleParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Component;

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
            long roleId = Long.parseLong(inputString);
            return context.getGuild().getRoleById(roleId);
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
