package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.VoiceChannelParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;

@Component
public class VoiceChannelParameterHandlerImpl implements VoiceChannelParameterHandler {
    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(VoiceChannel.class) && value.getType().equals(ParameterPieceType.STRING);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        String inputString = ((String) input.getValue()).trim();
        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(inputString);
        if(matcher.matches()) {
            long channelId = Long.parseLong(matcher.group(1));
            return context.getGuild().getVoiceChannelById(channelId);
        } else {
            if(NumberUtils.isParsable(inputString)) {
                long channelId = Long.parseLong(inputString);
                return context.getGuild().getVoiceChannelById(channelId);
            } else {
                List<VoiceChannel> possibleVoiceChannels = context.getGuild().getVoiceChannelsByName(inputString, true);
                if(possibleVoiceChannels.isEmpty()) {
                    throw new AbstractoTemplatedException("No channel found with name.", "no_channel_found_by_name_exception");
                }
                if(possibleVoiceChannels.size() > 1) {
                    throw new AbstractoTemplatedException("Multiple channels found with name.", "multiple_channels_found_by_name_exception");
                }
                return possibleVoiceChannels.get(0);
            }
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
