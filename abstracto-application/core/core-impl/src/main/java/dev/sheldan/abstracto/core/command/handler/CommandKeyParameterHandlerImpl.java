package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandParameterKey;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.CommandKeyParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
public class CommandKeyParameterHandlerImpl implements CommandKeyParameterHandler {

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return CommandParameterKey.class.isAssignableFrom(clazz) && value.getType().equals(ParameterPieceType.STRING);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        return CommandParameterKey.getEnumFromKey(param.getType(), ((String) input.getValue()).trim());
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }

}
