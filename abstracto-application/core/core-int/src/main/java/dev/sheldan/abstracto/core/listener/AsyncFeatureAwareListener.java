package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.FeatureAware;

public interface AsyncFeatureAwareListener<T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> extends AsyncAbstractoListener<T, R>, FeatureAware {
}
