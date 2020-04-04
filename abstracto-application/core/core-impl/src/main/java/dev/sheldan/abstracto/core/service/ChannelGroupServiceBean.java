package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelGroupServiceBean implements ChannelGroupService {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public AChannelGroup createChannelGroup(String name) {
        return channelGroupManagementService.createChannelGroup(name);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel) {
        addChannelToChannelGroup(channelGroupName, textChannel.getIdLong());
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, Long channelId) {
        AChannel aChannel = channelManagementService.loadChannel(channelId);
        addChannelToChannelGroup(channelGroupName, aChannel);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, AChannel channel) {
        AChannelGroup channelGroup = channelGroupManagementService.findByName(channelGroupName);
        channelGroupManagementService.addChannelToChannelGroup(channelGroup, channel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel) {
        removeChannelFromChannelGroup(channelGroupName, textChannel.getIdLong());
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, Long channelId) {
        AChannel aChannel = channelManagementService.loadChannel(channelId);
        removeChannelFromChannelGroup(channelGroupName, aChannel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, AChannel channel) {
        AChannelGroup channelGroup = channelGroupManagementService.findByName(channelGroupName);
        channelGroupManagementService.removeChannelFromChannelGroup(channelGroup, channel);
    }
}
