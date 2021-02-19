package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.exception.ChannelGroupNotFoundException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.ChannelGroupParameterHandler;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelGroupParameterHandlerImpl implements ChannelGroupParameterHandler {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        AServer server = serverManagementService.loadServer(context.getGuild().getIdLong());
        String inputString = (String) input.getValue();
        AChannelGroup actualInstance = channelGroupManagementService.findByNameAndServerOptional(inputString, server)
                .orElseThrow(() -> new ChannelGroupNotFoundException(inputString, channelGroupManagementService.getAllAvailableAsString(server)));
        ChannelGroupType channelGroupType = ChannelGroupType
                .builder()
                .id(actualInstance.getChannelGroupType().getId())
                .groupTypeKey(actualInstance.getChannelGroupType().getGroupTypeKey())
                .build();
        return AChannelGroup
                .builder()
                .id(actualInstance.getId())
                .groupName(actualInstance.getGroupName())
                .channelGroupType(channelGroupType)
                .build();
    }

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(AChannelGroup.class);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
