package dev.sheldan.abstracto.core.listener.entity;

import dev.sheldan.abstracto.core.models.database.AChannelGroup;

public interface ChannelGroupDeletedListener {
    void channelGroupDeleted(AChannelGroup channelGroup);
}
