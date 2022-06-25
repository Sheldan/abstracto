package dev.sheldan.abstracto.core.listener;

import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.CompletableFuture;

public interface ListenerService {
    <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> void executeFeatureAwareListener(FeatureAwareListener<T, R> listener, T model);
    <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> CompletableFuture<R> executeAsyncFeatureAwareListener(AsyncFeatureAwareListener<T, R> listener, T model);
    <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> void executeFeatureAwareListener(FeatureAwareListener<T, R> listener, T model, TaskExecutor executor);
    <T extends ListenerModel, R extends ListenerExecutionResult> void executeListener(AbstractoListener<T, R> listener, T model);
    <T extends ListenerModel, R extends ListenerExecutionResult> void executeListener(AbstractoListener<T, R> listener, T model, TaskExecutor executor);
}
