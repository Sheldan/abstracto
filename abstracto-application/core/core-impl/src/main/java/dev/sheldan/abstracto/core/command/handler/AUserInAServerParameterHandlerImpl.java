package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.handler.provided.AUserInAServerParameterHandler;
import dev.sheldan.abstracto.core.command.handler.provided.MemberParameterHandler;
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

    @Override
    public CompletableFuture handleAsync(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        CompletableFuture<AUserInAServer> future = new CompletableFuture<>();
        memberParameterHandler.handleAsync(input, iterators, Member.class, context).whenComplete((o, throwable) -> {
            try {
                AUserInAServer actualInstance;
                if (throwable == null) {
                    Member member = (Member) o;
                    actualInstance = userInServerManagementService.loadUser(member);
                } else {
                    Long userId = Long.parseLong(input);
                    actualInstance = userInServerManagementService.loadAUserInAServerOptional(context.getGuild().getIdLong(), userId).orElseThrow(() -> new UserInServerNotFoundException(0L));
                }
                future.complete(AUserInAServer.builder().userInServerId(actualInstance.getUserInServerId()).build());
            } catch (Exception e) {
                // we need to do it like this, because when complete only returns the exception it got in case two exceptions happen
                // so if the first exception happens in handleAsync, and we also throw one, it will _not_ get reported, because the other exception overshadows it
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(AUserInAServer.class);
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