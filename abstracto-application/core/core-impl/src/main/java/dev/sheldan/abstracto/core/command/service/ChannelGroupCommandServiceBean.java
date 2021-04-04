package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
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

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public Boolean isCommandEnabled(ACommand command, AChannel channel) {
        List<AChannelGroupCommand> allChannelGroupsOfCommand = channelGroupCommandService.getAllGroupCommandsForCommand(command);
        for (AChannelGroupCommand aChannelGroupCommand : allChannelGroupsOfCommand) {
            Optional<AChannel> channelInGroup = aChannelGroupCommand.getGroup()
                    .getChannels().stream().filter(innerChannel -> innerChannel.getId().equals(channel.getId())).findAny();
            if (channelInGroup.isPresent() && !aChannelGroupCommand.getEnabled()) {
                log.debug("Command {} is disabled because the channel is part of group {} in server.", command.getName(), aChannelGroupCommand.getGroup().getId());
                return false;
            }
        }
        /*
            if we are here this means either:
            the command has no channel groups assigned -> enabled
            the command has one or more channel group and is enabled in these ones -> enabled
            the command has a channel group with channels (and it might be enabled/disabled, does not matter), but the given channel is not part of that group -> ok
         */
        return true;
    }

    @Override
    public Boolean isCommandEnabled(ACommand command, Long channelId) {
        return isCommandEnabled(command, channelManagementService.loadChannel(channelId));
    }
}
