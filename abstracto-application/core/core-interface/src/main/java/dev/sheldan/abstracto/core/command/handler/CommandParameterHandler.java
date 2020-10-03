package dev.sheldan.abstracto.core.command.handler;


import net.dv8tion.jda.api.entities.Message;

public interface CommandParameterHandler {
    boolean handles(Class clazz);
    Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context);
    Integer getPriority();
}
