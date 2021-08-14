package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.AUserInAServerParameterHandler;
import dev.sheldan.abstracto.core.command.handler.provided.MemberParameterHandler;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AUserInAServerParameterHandlerImpl implements AUserInAServerParameterHandler {

    @Autowired
    private MemberParameterHandler memberParameterHandler;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private CommandService commandService;

    @Override
    public CompletableFuture handleAsync(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        CompletableFuture<AUserInAServer> future = new CompletableFuture<>();
        Parameter cloned = commandService.cloneParameter(param);
        cloned.setType(Member.class);
        memberParameterHandler.handleAsync(input, iterators, cloned, context, command).whenComplete((o, throwable) -> {
            AUserInAServer actualInstance;
            if (throwable == null) {
                Member member = (Member) o;
                actualInstance = userInServerManagementService.loadOrCreateUser(member);
            } else {
                Long userId = Long.parseLong(((String) input.getValue()).trim());
                actualInstance = userInServerManagementService.loadAUserInAServerOptional(context.getGuild().getIdLong(), userId).orElseThrow(() -> new UserInServerNotFoundException(0L));
            }
            future.complete(AUserInAServer.builder().userInServerId(actualInstance.getUserInServerId()).build());
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });
        return future;
    }

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(AUserInAServer.class) && value.getType().equals(ParameterPieceType.STRING);
    }

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
