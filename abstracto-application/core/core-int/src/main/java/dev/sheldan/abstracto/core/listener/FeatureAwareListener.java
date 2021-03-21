package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.FeatureAware;

public interface FeatureAwareListener<T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> extends AbstractoListener<T, R>, FeatureAware {
}
