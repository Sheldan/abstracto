package dev.sheldan.abstracto.core.listener.sync.entity;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelGroupCreatedListenerManager {
    @Autowired(required = false)
    private List<ChannelGroupCreatedListener> listener;

    @Autowired
    private ChannelGroupCreatedListenerManager self;

    public void executeListener(AChannelGroup createdGroup){
        listener.forEach(channelGroupCreatedListener ->
            channelGroupCreatedListener.channelGroupCreated(createdGroup)
        );
    }

}
