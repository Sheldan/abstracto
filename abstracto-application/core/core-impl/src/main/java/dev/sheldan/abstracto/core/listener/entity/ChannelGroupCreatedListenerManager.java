package dev.sheldan.abstracto.core.listener.entity;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelGroupCreatedListenerManager {
    @Autowired
    private List<ChannelGroupCreatedListener> listener;

    @Autowired
    private ChannelGroupCreatedListenerManager self;

    public void executeListener(AChannelGroup createdGroup){
        listener.forEach(channelGroupCreatedListener ->
            channelGroupCreatedListener.channelGroupCreated(createdGroup)
        );
    }

}
