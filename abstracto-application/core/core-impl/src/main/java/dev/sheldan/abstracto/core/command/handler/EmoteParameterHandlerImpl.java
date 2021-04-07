package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class EmoteParameterHandlerImpl implements EmoteParameterHandler {

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Emote.class);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        String inputString = (String) input.getValue();
        Matcher matcher = Message.MentionType.EMOTE.getPattern().matcher(inputString);
        if(matcher.matches()) {
            return iterators.getEmoteIterator().next();
        } else {
            if(StringUtils.isNumeric(inputString)) {
                long emoteId = Long.parseLong(inputString);
                return context.getGuild().getEmoteById(emoteId);
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
