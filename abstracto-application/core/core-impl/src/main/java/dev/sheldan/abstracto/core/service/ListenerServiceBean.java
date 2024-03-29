package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.listener.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ListenerServiceBean implements ListenerService {

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ListenerServiceBean self;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> void executeFeatureAwareListener(FeatureAwareListener<T, R> listener, T model) {
        if(model.getServerId() == null) {
            return;
        }
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, model.getServerId())) {
            return;
        }
        if(!featureModeService.necessaryFeatureModesMet(listener, model.getServerId())) {
            return;
        }
        try {
            listener.execute(model);
        } catch (Exception e) {
            log.error("Feature aware listener {} failed with exception:", listener.getClass().getName(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> CompletableFuture<R> executeAsyncFeatureAwareListener(AsyncFeatureAwareListener<T, R> listener, T model) {
        if(model.getServerId() == null) {
            return CompletableFuture.completedFuture(null);
        }
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, model.getServerId())) {
            return CompletableFuture.completedFuture(null);
        }
        if(!featureModeService.necessaryFeatureModesMet(listener, model.getServerId())) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            return listener.execute(model);
        } catch (Exception e) {
            log.error("Async feature aware listener {} failed with exception:", listener.getClass().getName(), e);
            CompletableFuture<R> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> void executeFeatureAwareListener(FeatureAwareListener<T, R> listener, T model, TaskExecutor executor) {
        if(model.getServerId() == null) {
            return;
        }
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, model.getServerId())) {
            return;
        }
        if(!featureModeService.necessaryFeatureModesMet(listener, model.getServerId())) {
            return;
        }
        try {
            CompletableFuture.runAsync(() -> self.executeFeatureListenerInTransaction(listener, model), executor).exceptionally(throwable -> {
                log.error("Feature aware async Listener {} failed with async exception:", listener.getClass().getName(), throwable);
                return null;
            });
        } catch (Exception e) {
            log.error("Feature aware listener {} failed with exception:", listener.getClass().getName(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public  <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> void executeFeatureListenerInTransaction(FeatureAwareListener<T, R> listener, T model) {
        listener.execute(model);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends ListenerModel, R extends ListenerExecutionResult> void executeListener(AbstractoListener<T, R> listener, T model) {
        try {
            listener.execute(model);
        } catch (Exception e) {
            log.error("Listener {} failed with exception:", listener.getClass().getName(), e);
        }
    }

    @Override
    public <T extends ListenerModel, R extends ListenerExecutionResult> void executeListener(AbstractoListener<T, R> listener, T model, TaskExecutor executor) {
        try {
            CompletableFuture.runAsync(() -> self.executeListenerInTransaction(listener, model), executor).exceptionally(throwable -> {
                log.error("Async Listener {} failed with async exception:", listener.getClass().getName(), throwable);
                return null;
            });
        } catch (Exception e) {
            log.error("Async listener {} failed with exception:", listener.getClass().getName(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends ListenerModel, R extends ListenerExecutionResult> void executeListenerInTransaction(AbstractoListener<T, R> listener, T model) {
        listener.execute(model);
    }

}
