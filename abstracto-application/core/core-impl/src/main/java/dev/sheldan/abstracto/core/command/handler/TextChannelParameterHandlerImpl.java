package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.TextChannelParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;

@Component
public class TextChannelParameterHandlerImpl implements TextChannelParameterHandler {
    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(TextChannel.class) && value.getType().equals(ParameterPieceType.STRING);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        String inputString = ((String) input.getValue()).trim();
        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(inputString);
        if(matcher.matches() && iterators.getChannelIterator().hasNext()) {
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
