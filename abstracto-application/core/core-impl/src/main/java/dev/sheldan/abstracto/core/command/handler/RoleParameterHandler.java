package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class RoleParameterHandler implements CommandParameterHandler {
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Role.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        Matcher matcher = Message.MentionType.ROLE.getPattern().matcher(input);
        if(matcher.matches()) {
            return iterators.getRoleIterator().next();
        } else {
            long roleId = Long.parseLong(input);
            return context.getGuild().getRoleById(roleId);
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
