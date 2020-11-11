package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.handler.provided.AChanelParameterHandler;
import dev.sheldan.abstracto.core.command.handler.provided.TextChannelParameterHandler;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.ChannelService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AChannelParameterHandlerImpl implements AChanelParameterHandler {

    @Autowired
    private TextChannelParameterHandler textChannelParameterHandler;

    @Autowired
    private ChannelService channelService;

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(AChannel.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        TextChannel textChannel = (TextChannel) textChannelParameterHandler.handle(input, iterators, clazz, context);
        return channelService.getFakeChannelFromTextChannel(textChannel);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
