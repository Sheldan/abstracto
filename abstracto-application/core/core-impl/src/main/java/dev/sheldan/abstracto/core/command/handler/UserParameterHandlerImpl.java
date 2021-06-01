package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.MemberParameterHandler;
import dev.sheldan.abstracto.core.command.handler.provided.UserParameterHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserParameterHandlerImpl implements UserParameterHandler {

    @Autowired
    private MemberParameterHandler memberParameterHandler;

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(User.class);
    }

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public CompletableFuture<Object> handleAsync(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        CompletableFuture<Object> memberParseFuture = memberParameterHandler.handleAsync(input, iterators, param, context, command);
        CompletableFuture<Object> mainFuture = new CompletableFuture<>();
        memberParseFuture.thenAccept(o -> mainFuture.complete(((Member)o).getUser())).exceptionally(throwable -> {
            String inputString = ((String) input.getValue()).trim();
            if(NumberUtils.isParsable(inputString)) {
                long userId = Long.parseLong(inputString);
                context.getGuild().getJDA().retrieveUserById(userId).queue(mainFuture::complete, mainFuture::completeExceptionally);
            } else {
                mainFuture.completeExceptionally(new AbstractoTemplatedException("No user found.", "no_user_found_exception"));
            }
            return null;
        });
        return mainFuture;
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
