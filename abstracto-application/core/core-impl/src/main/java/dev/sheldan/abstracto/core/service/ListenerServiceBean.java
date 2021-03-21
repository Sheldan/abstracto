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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> void executeFeatureAwareListener(FeatureAwareListener<T, R> listener, T model) {
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
    public <T extends FeatureAwareListenerModel, R extends ListenerExecutionResult> void executeFeatureAwareListener(FeatureAwareListener<T, R> listener, T model, TaskExecutor executor) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(listener.getFeature());
        if (!featureFlagService.isFeatureEnabled(feature, model.getServerId())) {
            return;
        }
        if(!featureModeService.necessaryFeatureModesMet(listener, model.getServerId())) {
            return;
        }
        try {
            CompletableFuture.runAsync(() -> listener.execute(model), executor).exceptionally(throwable -> {
                log.error("Feature aware async Listener {} failed with async exception:", listener.getClass().getName(), throwable);
                return null;
            });
        } catch (Exception e) {
            log.error("Feature aware listener {} failed with exception:", listener.getClass().getName(), e);
        }
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T extends ListenerModel, R extends ListenerExecutionResult> void executeListener(AbstractoListener<T, R> listener, T model, TaskExecutor executor) {
        try {
            CompletableFuture.runAsync(() -> listener.execute(model), executor).exceptionally(throwable -> {
                log.error("Async Listener {} failed with async exception:", listener.getClass().getName(), throwable);
                return null;
            });
        } catch (Exception e) {
            log.error("Async listener {} failed with exception:", listener.getClass().getName(), e);
        }
    }

}
