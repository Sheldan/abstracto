package dev.sheldan.abstracto.core.command.execution;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class ContextConverter {

    private ContextConverter() {

    }

    public static <T extends UserInitiatedServerContext> UserInitiatedServerContext fromCommandContext(CommandContext commandContext, Class<T> clazz)  {
        Method m = null;
        try {
            m = clazz.getMethod("builder");
            UserInitiatedServerContext.UserInitiatedServerContextBuilder<?, ?> builder = (UserInitiatedServerContext.UserInitiatedServerContextBuilder) m.invoke(null, null);
            return builder
                    .member(commandContext.getAuthor())
                    .guild(commandContext.getGuild())
                    .message(commandContext.getMessage())
                    .messageChannel(commandContext.getChannel())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new AbstractoRunTimeException("Failed to create UserInitiatedServerContext from CommandContext.");
    }

    public static <T extends SlimUserInitiatedServerContext> SlimUserInitiatedServerContext slimFromCommandContext(CommandContext commandContext, Class<T> clazz)  {
        Method m = null;
        try {
            m = clazz.getMethod("builder");
            SlimUserInitiatedServerContext.SlimUserInitiatedServerContextBuilder<?, ?> builder = (SlimUserInitiatedServerContext.SlimUserInitiatedServerContextBuilder) m.invoke(null, null);
            return builder
                    .member(commandContext.getAuthor())
                    .guild(commandContext.getGuild())
                    .message(commandContext.getMessage())
                    .channel(commandContext.getChannel())
                    .build();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to execute builder method", e);
        }
        throw new AbstractoRunTimeException("Failed to create SlimUserInitiatedServerContext from CommandContext");
    }
}
