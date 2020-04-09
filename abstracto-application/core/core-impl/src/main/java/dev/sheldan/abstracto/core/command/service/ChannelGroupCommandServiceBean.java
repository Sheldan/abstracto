package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.models.*;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementServiceBean;
import dev.sheldan.abstracto.core.models.converter.CommandConverter;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ChannelGroupCommandServiceBean implements ChannelGroupCommandService {

    @Autowired
    private ChannelGroupCommandManagementServiceBean channelGroupCommandService;

    @Autowired
    private CommandConverter commandConverter;

    @Override
    public Boolean isCommandEnabled(CommandDto command, ChannelDto channel) {
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
        return allChannelGroupsOfCommand.size() == 0;
    }
}
