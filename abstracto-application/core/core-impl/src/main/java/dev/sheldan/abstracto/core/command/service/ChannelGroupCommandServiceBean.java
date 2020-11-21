package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ChannelGroupCommandServiceBean implements ChannelGroupCommandService {

    @Autowired
    private ChannelGroupCommandManagementService channelGroupCommandService;

    @Override
    public Boolean isCommandEnabled(ACommand command, AChannel channel) {
        List<AChannelGroupCommand> allChannelGroupsOfCommand = channelGroupCommandService.getAllGroupCommandsForCommand(command);
        for (AChannelGroupCommand aChannelGroupCommand : allChannelGroupsOfCommand) {
            Optional<AChannel> channelInGroup = aChannelGroupCommand.getGroup()
                    .getChannels().stream().filter(innerChannel -> innerChannel.getId().equals(channel.getId())).findAny();
            if (channelInGroup.isPresent() && !aChannelGroupCommand.getEnabled()) {
                log.trace("Command {} is disabled because the channel is part of group {} in server.", command.getName(), aChannelGroupCommand.getGroup().getId());
                return false;
            }
        }
        // empty -> no groups, command enabled
        // not empty -> has groups, command is disabled in all
        return allChannelGroupsOfCommand.isEmpty();
    }
}
