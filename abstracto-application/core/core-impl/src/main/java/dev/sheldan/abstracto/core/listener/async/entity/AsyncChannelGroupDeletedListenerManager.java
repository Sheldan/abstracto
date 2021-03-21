package dev.sheldan.abstracto.core.listener.async.entity;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.listener.sync.entity.AsyncChannelGroupDeletedListener;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupDeletedListenerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class AsyncChannelGroupDeletedListenerManager {
    @Autowired(required = false)
    private List<AsyncChannelGroupDeletedListener> listener;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    @Qualifier("channelGroupDeletedExecutor")
    private TaskExecutor channelGroupDeletedExecutor;

    @TransactionalEventListener
    public void executeListener(ChannelGroupDeletedListenerModel model){
        listener.forEach(channelGroupCreatedListener ->
            listenerService.executeListener(channelGroupCreatedListener, model, channelGroupDeletedExecutor)
        );
    }

}
