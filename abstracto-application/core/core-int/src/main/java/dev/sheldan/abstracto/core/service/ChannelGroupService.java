package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.models.template.commands.ChannelGroupModel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public interface ChannelGroupService {
    AChannelGroup createChannelGroup(String name, Long serverId, ChannelGroupType channelGroupType);
    void deleteChannelGroup(String name, Long serverId);
    void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel);
    void addChannelToChannelGroup(String channelGroupName, Long channelId, Long serverId);
    void addChannelToChannelGroup(String channelGroupName, AChannel channel);
    void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel);
    void removeChannelFromChannelGroup(String channelGroupName, Long channelId, Long serverId);
    void removeChannelFromChannelGroup(String channelGroupName, AChannel channel);
    void addCommandToChannelGroup(String commandName, String channelGroupName, Long serverId);
    void removeCommandFromChannelGroup(String commandName, String channelGroupName, Long serverId);
    void disableChannelGroup(String channelGroupName, Long serverId);
    void enableChannelGroup(String channelGroupName, Long serverId);
    boolean doesGroupExist(String groupName, Long serverId);
    List<AChannelGroup> getChannelGroupsOfChannelWithType(AChannel channel, String groupTypeKey);
    List<ChannelGroupModel> convertAChannelGroupToChannelGroupChannel(List<AChannelGroup> channelGroups);
}
