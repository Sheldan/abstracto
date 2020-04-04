package dev.sheldan.abstracto.command.execution;

import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class ContextConverter {

    public static <T extends UserInitiatedServerContext> UserInitiatedServerContext fromCommandContext(CommandContext commandContext, Class<T> clazz)  {
        Method m = null;
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            return builder
                    .member(commandContext.getAuthor())
                    .guild(commandContext.getGuild())
                    .messageChannel(commandContext.getChannel())
                    .channel(commandContext.getUserInitiatedContext().getChannel())
                    .server(commandContext.getUserInitiatedContext().getServer())
                    .aUserInAServer(commandContext.getUserInitiatedContext().getAUserInAServer())
                    .user(commandContext.getUserInitiatedContext().getUser())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new RuntimeException("Failed to create model from context");
    }
}
