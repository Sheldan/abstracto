package dev.sheldan.abstracto.core.listener.sync.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupDeletedListenerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SyncChannelGroupDeletedListenerManager {
    @Autowired(required = false)
    private List<ChannelGroupDeletedListener> listener;

    @Autowired
    private ListenerService listenerService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeListener(ChannelGroupDeletedListenerModel model){
        listener.forEach(channelGroupCreatedListener ->
            listenerService.executeListener(channelGroupCreatedListener, model)
        );
    }

}
