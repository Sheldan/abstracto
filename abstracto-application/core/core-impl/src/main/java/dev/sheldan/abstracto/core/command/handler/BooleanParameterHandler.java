package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
public class BooleanParameterHandler implements CommandParameterHandler {
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Boolean.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        return Boolean.valueOf(input);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
