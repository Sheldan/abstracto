package dev.sheldan.abstracto.core.listener.entity;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelGroupDeletedListenerManager {
    @Autowired
    private List<ChannelGroupDeletedListener> listener;

    public void executeListener(AChannelGroup createdGroup){
        listener.forEach(channelGroupCreatedListener ->
            channelGroupCreatedListener.channelGroupDeleted(createdGroup)
        );
    }

}
