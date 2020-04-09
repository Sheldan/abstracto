package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ChannelGroupDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public interface ChannelGroupService {
    ChannelGroupDto createChannelGroup(String name, Long serverId);
    void deleteChannelGroup(String name, Long serverId);
    void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel);
    void addChannelToChannelGroup(String channelGroupName, Long channelId);
    void addChannelToChannelGroup(String channelGroupName, ChannelDto channel);
    void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel);
    void removeChannelFromChannelGroup(String channelGroupName, Long channelId);
    void removeChannelFromChannelGroup(String channelGroupName, ChannelDto channel);
    void disableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId);
    void enableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId);
    boolean doesGroupExist(String groupName, Long serverId);
    List<ChannelGroupDto> findAllInServer(ServerDto server);
}
