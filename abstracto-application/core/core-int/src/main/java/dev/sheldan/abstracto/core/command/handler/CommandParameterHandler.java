package dev.sheldan.abstracto.core.command.handler;


import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface CommandParameterHandler {
    boolean handles(Class clazz, UnparsedCommandParameterPiece value);
    default boolean async() { return false; }
    default Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) { return new Object();}
    default CompletableFuture<Object> handleAsync(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) { return CompletableFuture.completedFuture(null); }
    Integer getPriority();
}
