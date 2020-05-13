package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import net.dv8tion.jda.api.entities.TextChannel;

public interface ChannelGroupService {
    AChannelGroup createChannelGroup(String name, Long serverId);
    void deleteChannelGroup(String name, Long serverId);
    void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel);
    void addChannelToChannelGroup(String channelGroupName, Long channelId, Long serverId);
    void addChannelToChannelGroup(String channelGroupName, AChannel channel);
    void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel);
    void removeChannelFromChannelGroup(String channelGroupName, Long channelId, Long serverId);
    void removeChannelFromChannelGroup(String channelGroupName, AChannel channel);
    void disableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId);
    void enableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId);
    boolean doesGroupExist(String groupName, Long serverId);
}
