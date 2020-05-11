package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.List;

public interface ChannelGroupManagementService {
    AChannelGroup createChannelGroup(String name, AServer server);
    boolean doesChannelGroupExist(String name, AServer server);
    void deleteChannelGroup(String name, AServer server);
    AChannelGroup addChannelToChannelGroup(AChannelGroup channelGroup, AChannel channel);
    void removeChannelFromChannelGroup(AChannelGroup channelGroup, AChannel channel);
    AChannelGroup findByNameAndServer(String name, AServer server);
    List<AChannelGroup> findAllInServer(AServer server);
    List<AChannelGroup> findAllInServer(Long serverId);
}
