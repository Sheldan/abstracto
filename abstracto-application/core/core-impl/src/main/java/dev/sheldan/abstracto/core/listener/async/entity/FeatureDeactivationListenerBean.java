package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.FeatureDeactivationListenerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class FeatureDeactivationListenerBean {
    @Autowired(required = false)
    private List<FeatureDeactivationListener> listener;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("featureActivationExecutor")
    private TaskExecutor featureDeactivationExecutor;

    @TransactionalEventListener
    public void executeListener(FeatureDeactivationListenerModel deactivatedFeature){
        if(listener == null) return;
        listener.forEach(featureActivatedListener ->
                listenerService.executeListener(featureActivatedListener, deactivatedFeature, featureDeactivationExecutor)
        );
    }
}
