package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
public class MemberParameterHandler implements CommandParameterHandler {
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Member.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        Matcher matcher = Message.MentionType.USER.getPattern().matcher(input);
        if(matcher.matches()) {
            return iterators.getMemberIterator().next();
        } else {
            // TODO add handling for names
            long emoteId = Long.parseLong(input);
            return context.getGuild().getMemberById(emoteId);
        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
