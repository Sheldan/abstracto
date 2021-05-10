package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;

import java.util.List;
import java.util.Optional;

public interface ChannelGroupManagementService {
    AChannelGroup createChannelGroup(String name, AServer server, ChannelGroupType channelGroupType);
    boolean doesChannelGroupExist(String name, AServer server);
    void deleteChannelGroup(String name, AServer server);
    AChannelGroup addChannelToChannelGroup(AChannelGroup channelGroup, AChannel channel);
    Optional<AChannelGroup> findChannelGroupByIdOptional(Long channelGroupId);
    AChannelGroup findChannelGroupById(Long channelGroupId);
    void removeChannelFromChannelGroup(AChannelGroup channelGroup, AChannel channel);
    AChannelGroup findByNameAndServer(String name, AServer server);
    Optional<AChannelGroup> findByNameAndServerOptional(String name, AServer server);
    AChannelGroup findByNameAndServerAndType(String name, AServer server, String expectedType);
    List<AChannelGroup> findAllInServer(AServer server);
    List<String> getAllAvailableAsString(AServer server);
    List<AChannelGroup> findAllInServer(Long serverId);
    List<AChannelGroup> getAllChannelGroupsOfChannel(AChannel channel);
    List<AChannelGroup> findAllInServerWithType(Long serverId, String type);
    boolean isChannelInEnabledChannelGroupOfType(String channelGroupType, Long channelId);
}
