package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import dev.sheldan.abstracto.core.command.handler.provided.CommandKeyParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
public class CommandKeyParameterHandlerImpl implements CommandKeyParameterHandler {

    @Override
    public boolean handles(Class clazz) {
        return CommandParameterKey.class.isAssignableFrom(clazz);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        return CommandParameterKey.getEnumFromKey(clazz, input);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }

}
