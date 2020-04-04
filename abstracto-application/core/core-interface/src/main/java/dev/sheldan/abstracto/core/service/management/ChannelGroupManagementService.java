package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;

public interface ChannelGroupManagementService {
    AChannelGroup createChannelGroup(String name);
    AChannelGroup addChannelToChannelGroup(AChannelGroup channelGroup, AChannel channel);
    void removeChannelFromChannelGroup(AChannelGroup channelGroup, AChannel channel);
    AChannelGroup findByName(String name);
}
