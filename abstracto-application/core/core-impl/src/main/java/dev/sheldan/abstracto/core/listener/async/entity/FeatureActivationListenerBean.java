package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.FeatureActivationListenerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class FeatureActivationListenerBean {
    @Autowired(required = false)
    private List<FeatureActivationListener> listener;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("featureActivationExecutor")
    private TaskExecutor featureActivationExecutor;

    @TransactionalEventListener
    public void executeListener(FeatureActivationListenerModel activatedFeature){
        if(listener == null) return;
        listener.forEach(featureActivatedListener ->
                listenerService.executeListener(featureActivatedListener, activatedFeature, featureActivationExecutor)
        );
    }
}
