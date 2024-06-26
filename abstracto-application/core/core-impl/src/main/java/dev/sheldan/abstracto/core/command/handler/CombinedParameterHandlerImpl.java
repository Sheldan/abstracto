package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.CombinedParameterEntry;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.parameter.CombinedParameter;
import dev.sheldan.abstracto.core.command.handler.provided.CombinedParametersHandler;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
@Slf4j
public class CombinedParameterHandlerImpl implements CombinedParametersHandler {

    @Autowired
    @Lazy
    private List<CommandParameterHandler> parameterHandlers;

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public CompletableFuture<Object> handleAsync(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        List<CompletableFuture<Object>> futures = new ArrayList<>();
        CompletableFuture<Object> returningFuture = new CompletableFuture<>();
        List<CombinedParameterEntry> combinedParameterEntries = (List) param.getAdditionalInfo().get(Parameter.ADDITIONAL_TYPES_KEY);
        List<Class> possibleTypes = combinedParameterEntries
                .stream()
                .filter(CombinedParameterEntry::isUsableInMessageCommands)
                .map(CombinedParameterEntry::getType)
                .toList();
        for (Class concreteParameter: possibleTypes) {
            for (CommandParameterHandler handler : parameterHandlers) {
                try {
                    if (handler.handles(concreteParameter, input)) {
                        if (handler.async()) {
                            futures.add(handler.handleAsync(input, iterators, param, context, command));
                        } else {
                            Object result = handler.handle(input, iterators, param, context, command);
                            futures.add(CompletableFuture.completedFuture(result));
                        }
                    }
                } catch (Exception e) {
                    futures.add(CompletableFuture.failedFuture(e));
                }
            }
        }
        FutureUtils.toSingleFutureGeneric(futures).whenComplete((unused, throwable) -> {
            for (CompletableFuture<Object> future: futures) {
                if(!future.isCompletedExceptionally()) {
                    Object value = future.join();
                    if(value != null) {
                        returningFuture.complete(value);
                        return;
                    }
                }
            }
            returningFuture.completeExceptionally(new IncorrectParameterException(command, param.getName()));
        }).exceptionally(throwable -> {
            returningFuture.completeExceptionally(throwable);
            return null;
        });
        return returningFuture;
    }

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(CombinedParameter.class);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
