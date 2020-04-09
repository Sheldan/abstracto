package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.exception.ChannelGroupException;
import dev.sheldan.abstracto.core.command.exception.CommandException;
import dev.sheldan.abstracto.core.models.*;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementServiceBean;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementServiceBean;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.core.models.converter.ChannelGroupConverter;
import dev.sheldan.abstracto.core.models.converter.ServerConverter;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ChannelGroupDto;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementServiceBean;
import dev.sheldan.abstracto.core.service.management.ChannelManagementServiceBean;
import dev.sheldan.abstracto.core.service.management.ServerManagementServiceBean;
import net.dv8tion.jda.api.entities.TextChannel;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChannelGroupServiceBean implements ChannelGroupService {

    @Autowired
    private ChannelGroupManagementServiceBean channelGroupManagementService;

    @Autowired
    private ChannelManagementServiceBean channelManagementService;

    @Autowired
    private CommandManagementServiceBean commandManagementService;

    @Autowired
    private ChannelGroupCommandManagementServiceBean channelGroupCommandManagementService;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    @Autowired
    private ChannelGroupConverter channelGroupConverter;

    @Autowired
    private ChannelConverter channelConverter;

    @Autowired
    private ServerConverter serverConverter;

    @Override
    public ChannelGroupDto createChannelGroup(String name, Long serverId) {
        ServerDto server = serverManagementService.loadOrCreate(serverId);

        AChannelGroup channelGroup = channelGroupManagementService.createChannelGroup(name, server);
        return channelGroupConverter.fromChannelGroup(channelGroup);
    }

    @Override
    public void deleteChannelGroup(String name, Long serverId) {
        ServerDto server = serverManagementService.loadOrCreate(serverId);
        channelGroupManagementService.deleteChannelGroup(name, server);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel) {
        addChannelToChannelGroup(channelGroupName, textChannel.getIdLong());
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, Long channelId) {
        ChannelDto aChannel = ChannelDto.builder().id(channelId).build();
        addChannelToChannelGroup(channelGroupName, aChannel);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, ChannelDto channel) {
        ServerDto server = serverManagementService.loadOrCreate(channel.getServer().getId());
        ChannelGroupDto channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        if(channelGroup == null) {
            throw new ChannelGroupException(String.format("Channel group %s was not found.", channelGroupName));
        }
        channelGroupManagementService.addChannelToChannelGroup(channelGroup, channel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel) {
        removeChannelFromChannelGroup(channelGroupName, textChannel.getIdLong());
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, Long channelId) {
        ChannelDto channel = ChannelDto.builder().id(channelId).build();
        removeChannelFromChannelGroup(channelGroupName, channel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, ChannelDto channel) {
        ServerDto serverDto = ServerDto.builder().id(channel.getServer().getId()).build();
        ChannelGroupDto channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, serverDto);
        if(channelGroup == null) {
            throw new ChannelGroupException(String.format("Channel group %s was not found", channelGroupName));
        }
        channelGroupManagementService.removeChannelFromChannelGroup(channelGroup, channel);
    }

    @Override
    public void disableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId) {
        ServerDto serverDto = ServerDto.builder().id(serverId).build();
        ChannelGroupDto channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, serverDto);
        if(channelGroup == null) {
            throw new ChannelGroupException(String.format("Channel group %s was not found", channelGroupName));
        }
        CommandDto command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandException(String.format("Command %s not found.", commandName));
        }
        channelGroupCommandManagementService.setCommandInGroupTo(command, channelGroup, false);
    }

    @Override
    public void enableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId) {
        ServerDto serverDto = ServerDto.builder().id(serverId).build();
        ChannelGroupDto channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, serverDto);
        if(channelGroup == null) {
            throw new ChannelGroupException(String.format("Channel group %s was not found", channelGroupName));
        }
        CommandDto command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandException(String.format("Command %s not found.", commandName));
        }
        channelGroupCommandManagementService.setCommandInGroupTo(command, channelGroup, true);
    }

    @Override
    public boolean doesGroupExist(String groupName, Long serverId) {
        ServerDto server = serverManagementService.loadOrCreate(serverId);
        return channelGroupManagementService.findByNameAndServer(groupName, server) != null;
    }

    @Override
    public List<ChannelGroupDto> findAllInServer(ServerDto server) {
        List<ChannelGroupDto> channelGroupDtos = new ArrayList<>();
        List<AChannelGroup> allInServer = channelGroupManagementService.findAllInServer(server.getId());
        allInServer.forEach(channelGroup -> {
            channelGroupDtos.add(channelGroupConverter.fromChannelGroup(channelGroup));
        });
        return channelGroupDtos;
    }
}
