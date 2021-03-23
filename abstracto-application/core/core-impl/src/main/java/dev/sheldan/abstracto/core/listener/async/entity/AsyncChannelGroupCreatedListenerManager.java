package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.listener.sync.entity.AsyncChannelGroupCreatedListener;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupCreatedListenerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class AsyncChannelGroupCreatedListenerManager {
    @Autowired(required = false)
    private List<AsyncChannelGroupCreatedListener> listener;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("channelGroupCreatedExecutor")
    private TaskExecutor channelGroupCreatedExecutor;

    @TransactionalEventListener
    public void executeListener(ChannelGroupCreatedListenerModel createdGroup){
        if(listener == null) return;
        listener.forEach(asyncChannelGroupCreatedListener ->
            listenerService.executeListener(asyncChannelGroupCreatedListener, createdGroup, channelGroupCreatedExecutor)
        );
    }

}
