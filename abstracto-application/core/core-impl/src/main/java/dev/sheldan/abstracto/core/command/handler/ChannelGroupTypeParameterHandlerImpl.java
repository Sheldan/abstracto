package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.ChannelGroupTypeParameterHandler;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.service.management.ChannelGroupTypeManagementService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelGroupTypeParameterHandlerImpl implements ChannelGroupTypeParameterHandler {

    @Autowired
    private ChannelGroupTypeManagementService channelGroupTypeManagementService;

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(ChannelGroupType.class) && value.getType().equals(ParameterPieceType.STRING);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        ChannelGroupType actualGroupType = channelGroupTypeManagementService.findChannelGroupTypeByKey(((String) input.getValue()).trim());
        return ChannelGroupType
                .builder()
                .groupTypeKey(actualGroupType.getGroupTypeKey())
                .id(actualGroupType.getId())
                .build();
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
