package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.core.command.CommandConstants.COMMAND_CHANNEL_GROUP_KEY;

@Component
public class CommandDisabledServiceBean implements CommandDisabledService {

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ChannelGroupCommandManagementService channelGroupCommandManagementService;

    @Override
    public void disableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        ACommand command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandNotFoundException();
        }
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServerAndType(channelGroupName, server, COMMAND_CHANNEL_GROUP_KEY);
        channelGroupCommandManagementService.setCommandInGroupTo(command, channelGroup, false);
    }

    @Override
    public void enableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        ACommand command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandNotFoundException();
        }
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServerAndType(channelGroupName, server, COMMAND_CHANNEL_GROUP_KEY);
        channelGroupCommandManagementService.setCommandInGroupTo(command, channelGroup, true);
    }
}
