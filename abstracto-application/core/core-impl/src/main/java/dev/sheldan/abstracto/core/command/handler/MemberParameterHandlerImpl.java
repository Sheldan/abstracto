package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.MemberParameterHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

@Component
public class MemberParameterHandlerImpl implements MemberParameterHandler {
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Member.class);
    }

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public CompletableFuture<Object> handleAsync(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        String inputString = (String) input.getValue();
        Matcher matcher = Message.MentionType.USER.getPattern().matcher(inputString);
        if(matcher.matches()) {
            return CompletableFuture.completedFuture(iterators.getMemberIterator().next());
        } else {
            if(NumberUtils.isParsable(inputString)) {
                long userId = Long.parseLong(inputString);
                return context.getGuild().retrieveMemberById(userId).submit().thenApply(member -> member);
            } else {
                List<Member> possibleMembers = context.getGuild().getMembersByName(inputString, true);
                if(possibleMembers.isEmpty()) {
                    throw new AbstractoTemplatedException("No member found with name.", "no_member_found_by_name_exception");
                }
                if(possibleMembers.size() > 1) {
                    throw new AbstractoTemplatedException("Multiple members found with name.", "multiple_members_found_by_name_exception");
                }
                return CompletableFuture.completedFuture(possibleMembers.get(0));
            }

        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
