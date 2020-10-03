package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class EmoteParameterHandler implements CommandParameterHandler {

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Emote.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        Matcher matcher = Message.MentionType.EMOTE.getPattern().matcher(input);
        if(matcher.matches()) {
            return iterators.getEmoteIterator().next();
        } else {
            if(StringUtils.isNumeric(input)) {
                long emoteId = Long.parseLong(input);
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
