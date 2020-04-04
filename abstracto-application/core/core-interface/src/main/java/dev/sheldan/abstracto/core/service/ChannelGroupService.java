package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import net.dv8tion.jda.api.entities.TextChannel;

public interface ChannelGroupService {
    AChannelGroup createChannelGroup(String name);
    void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel);
    void addChannelToChannelGroup(String channelGroupName, Long channelId);
    void addChannelToChannelGroup(String channelGroupName, AChannel channel);
    void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel);
    void removeChannelFromChannelGroup(String channelGroupName, Long channelId);
    void removeChannelFromChannelGroup(String channelGroupName, AChannel channel);
}
