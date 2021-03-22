package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.exception.ChannelGroupNotFoundException;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static dev.sheldan.abstracto.core.command.CommandConstants.COMMAND_CHANNEL_GROUP_KEY;

@Component
public class ChannelGroupServiceBean implements ChannelGroupService {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private ChannelGroupCommandManagementService channelGroupCommandManagementService;

    @Autowired
    private ServerManagementService serverManagementService;


    @Override
    public AChannelGroup createChannelGroup(String name, Long serverId, ChannelGroupType channelGroupType) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return channelGroupManagementService.createChannelGroup(name, server, channelGroupType);
    }

    @Override
    public void deleteChannelGroup(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        channelGroupManagementService.deleteChannelGroup(name, server);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel) {
        addChannelToChannelGroup(channelGroupName, textChannel.getIdLong(), textChannel.getGuild().getIdLong());
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, Long channelId, Long serverId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        addChannelToChannelGroup(channelGroupName, channel);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, AChannel channel) {
        AServer server = serverManagementService.loadOrCreate(channel.getServer().getId());
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        if(channelGroup == null) {
            throw new ChannelGroupNotFoundException(channelGroupName, channelGroupManagementService.getAllAvailableAsString(server));
        }
        channelGroupManagementService.addChannelToChannelGroup(channelGroup, channel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel) {
        removeChannelFromChannelGroup(channelGroupName, textChannel.getIdLong(), textChannel.getGuild().getIdLong());
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, Long channelId, Long serverId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        removeChannelFromChannelGroup(channelGroupName, channel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, AChannel channel) {
        AServer server = serverManagementService.loadOrCreate(channel.getServer().getId());
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        if(channelGroup == null) {
            throw new ChannelGroupNotFoundException(channelGroupName, channelGroupManagementService.getAllAvailableAsString(server));
        }
        channelGroupManagementService.removeChannelFromChannelGroup(channelGroup, channel);
    }

    @Override
    public void disableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServerAndType(channelGroupName, server, COMMAND_CHANNEL_GROUP_KEY);
        ACommand command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandNotFoundException();
        }
        channelGroupCommandManagementService.setCommandInGroupTo(command, channelGroup, false);
    }

    @Override
    public void enableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServerAndType(channelGroupName, server, COMMAND_CHANNEL_GROUP_KEY);
        ACommand command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandNotFoundException();
        }
        channelGroupCommandManagementService.setCommandInGroupTo(command, channelGroup, true);
    }

    @Override
    public boolean doesGroupExist(String groupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return channelGroupManagementService.findByNameAndServer(groupName, server) != null;
    }

    @Override
    public List<AChannelGroup> getChannelGroupsOfChannelWithType(AChannel channel, String groupTypeKey) {
        List<AChannelGroup> channelGroups = channelGroupManagementService.getAllChannelGroupsOfChannel(channel);
        return channelGroups
                .stream()
                .filter(aChannelGroup -> aChannelGroup.getChannelGroupType().getGroupTypeKey().equals(groupTypeKey))
                .collect(Collectors.toList());
    }
}
