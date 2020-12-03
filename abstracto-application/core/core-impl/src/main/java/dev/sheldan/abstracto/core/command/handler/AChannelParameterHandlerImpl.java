package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
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
    public boolean handles(Class clazz) {
        return clazz.equals(AChannel.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        TextChannel textChannel = (TextChannel) textChannelParameterHandler.handle(input, iterators, clazz, context);
        if(textChannel == null) {
            Long channelId = Long.parseLong(input);
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
