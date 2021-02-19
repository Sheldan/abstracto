package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.LongParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
public class LongParameterHandlerImpl implements LongParameterHandler {

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Long.class);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        return Long.parseLong((String) input.getValue());
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }

}
