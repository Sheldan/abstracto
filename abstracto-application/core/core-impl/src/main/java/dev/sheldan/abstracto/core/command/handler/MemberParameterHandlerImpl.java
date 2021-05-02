package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.MemberParameterHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

@Component
public class MemberParameterHandlerImpl implements MemberParameterHandler {
    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(Member.class);
    }

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public CompletableFuture<Object> handleAsync(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        String inputString = ((String) input.getValue()).trim();
        Matcher matcher = Message.MentionType.USER.getPattern().matcher(inputString);
        if(matcher.matches() && iterators.getMemberIterator().hasNext()) {
            return CompletableFuture.completedFuture(iterators.getMemberIterator().next());
        } else {
            if(NumberUtils.isParsable(inputString)) {
                long userId = Long.parseLong(inputString);
                return context.getGuild().retrieveMemberById(userId).submit().thenApply(member -> member);
            } else {
                Task<List<Member>> listTask = context.getGuild().retrieveMembersByPrefix(inputString, 1);
                CompletableFuture<Object> memberFuture = new CompletableFuture<>();
                listTask.onSuccess(members -> {
                    if(members.isEmpty()) {
                        memberFuture.completeExceptionally(new AbstractoTemplatedException("No member found with name.", "no_member_found_by_name_exception"));
                    } else if(members.size() > 1) {
                        memberFuture.completeExceptionally(new AbstractoTemplatedException("Multiple members found with name.", "multiple_members_found_by_name_exception"));
                    } else {
                        memberFuture.complete(members.get(0));
                    }
                });
                listTask.onError(memberFuture::completeExceptionally);
                return memberFuture;
            }

        }
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
