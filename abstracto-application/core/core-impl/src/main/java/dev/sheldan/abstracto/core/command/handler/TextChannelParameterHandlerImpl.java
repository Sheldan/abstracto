package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.TextChannelParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
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
            if(NumberUtils.isParsable(inputString)) {
                long channelId = Long.parseLong(inputString);
                return context.getGuild().getTextChannelById(channelId);
            } else {
                List<TextChannel> possibleTextChannels = context.getGuild().getTextChannelsByName(inputString, true);
                if(possibleTextChannels.isEmpty()) {
                    throw new AbstractoTemplatedException("No channel found with name.", "no_channel_found_by_name_exception");
                }
                if(possibleTextChannels.size() > 1) {
                    throw new AbstractoTemplatedException("Multiple channels found with name.", "multiple_channels_found_by_name_exception");
                }
                return possibleTextChannels.get(0);
            }
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
