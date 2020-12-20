package dev.sheldan.abstracto.core.listener.sync.entity;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;

public interface ChannelGroupCreatedListener {
    void channelGroupCreated(AChannelGroup channelGroup);
}
