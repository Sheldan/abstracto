package dev.sheldan.abstracto.core.command.handler;


import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface CommandParameterHandler {
    boolean handles(Class clazz);
    default boolean async() { return false; }
    default Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) { return new Object();}
    default CompletableFuture<Object> handleAsync(String input, CommandParameterIterators iterators, Class clazz, Message context) { return CompletableFuture.completedFuture(null); }
    Integer getPriority();
}
