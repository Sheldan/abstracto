package dev.sheldan.abstracto.core.listener;

import java.util.concurrent.CompletableFuture;

public interface AsyncAbstractoListener<M extends ListenerModel, R extends ListenerExecutionResult> {
    CompletableFuture<R> execute(M model);
}
