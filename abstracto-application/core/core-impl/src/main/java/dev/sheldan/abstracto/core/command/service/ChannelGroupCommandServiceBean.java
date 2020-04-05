package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.models.ACommand;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ChannelGroupCommandServiceBean implements ChannelGroupCommandService {

    @Autowired
    private ChannelGroupCommandManagementService channelGroupCommandService;

    @Override
    public Boolean isCommandEnabled(ACommand command, AChannel channel) {
        List<AChannelGroupCommand> allChannelGroupsOfCommand = channelGroupCommandService.getAllGroupCommandsForCommand(command);
        for (AChannelGroupCommand aChannelGroupCommand : allChannelGroupsOfCommand) {
            Optional<AChannel> channelInGroup = aChannelGroupCommand.getGroup()
                    .getChannels().stream().filter(channel1 -> channel1.getId().equals(channel.getId())).findAny();
            if (channelInGroup.isPresent()) {
                if (aChannelGroupCommand.getEnabled()) {
                    return true;
                }
            }
        }
        if(allChannelGroupsOfCommand.size() == 0) {
            return true;
        }
        return false;
    }
}
