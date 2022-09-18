package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class EmoteParameterHandlerImpl implements EmoteParameterHandler {

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(CustomEmoji.class) && value.getType().equals(ParameterPieceType.STRING);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        String inputString = ((String) input.getValue()).trim();
        Matcher matcher = Message.MentionType.EMOJI.getPattern().matcher(inputString);
        if(matcher.matches() && iterators.getEmoteIterator().hasNext()) {
            return iterators.getEmoteIterator().next();
        } else {
            if(StringUtils.isNumeric(inputString)) {
                long emoteId = Long.parseLong(inputString);
                return context.getGuild().getEmojiById(emoteId);
            } else {
                return null;
            }
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
