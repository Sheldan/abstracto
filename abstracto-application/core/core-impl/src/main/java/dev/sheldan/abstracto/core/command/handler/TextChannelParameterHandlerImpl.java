package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.TextChannelParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class TextChannelParameterHandlerImpl implements TextChannelParameterHandler {
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(TextChannel.class);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        String inputString = (String) input.getValue();
        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(inputString);
        if(matcher.matches()) {
            return iterators.getChannelIterator().next();
        } else {
            long channelId = Long.parseLong(inputString);
            return context.getGuild().getTextChannelById(channelId);
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
