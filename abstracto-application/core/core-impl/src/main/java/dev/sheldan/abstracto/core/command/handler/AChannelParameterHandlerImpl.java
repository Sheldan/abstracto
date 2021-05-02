package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.AChannelParameterHandler;
import dev.sheldan.abstracto.core.command.handler.provided.TextChannelParameterHandler;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AChannelParameterHandlerImpl implements AChannelParameterHandler {

    @Autowired
    private TextChannelParameterHandler textChannelParameterHandler;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(AChannel.class);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        TextChannel textChannel = (TextChannel) textChannelParameterHandler.handle(input, iterators, param, context, command);
        if(textChannel == null) {
            Long channelId = Long.parseLong(((String) input.getValue()).trim());
            AChannel actualInstance = channelManagementService.loadChannel(channelId);
            return AChannel.builder().fake(true).id(actualInstance.getId()).build();
        } else {
            return channelService.getFakeChannelFromTextChannel(textChannel);
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
