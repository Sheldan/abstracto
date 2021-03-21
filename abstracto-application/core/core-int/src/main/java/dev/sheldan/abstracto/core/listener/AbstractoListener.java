package dev.sheldan.abstracto.core.listener;

public interface AbstractoListener<M extends ListenerModel, R extends ListenerExecutionResult> {
    R execute(M model);
}
