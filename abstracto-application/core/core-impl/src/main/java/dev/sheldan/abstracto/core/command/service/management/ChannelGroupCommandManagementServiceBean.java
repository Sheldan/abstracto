package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.models.AChannelGroup;
import dev.sheldan.abstracto.core.models.AChannelGroupCommand;
import dev.sheldan.abstracto.core.models.ACommand;
import dev.sheldan.abstracto.core.command.repository.ChannelGroupCommandRepository;
import dev.sheldan.abstracto.core.models.dto.ChannelGroupDto;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ChannelGroupCommandManagementServiceBean  {

    @Autowired
    private ChannelGroupCommandRepository groupCommandRepository;

    public void setCommandInGroupTo(CommandDto command, ChannelGroupDto group, Boolean enabled) {
        AChannelGroup aChannelGroup = AChannelGroup.builder().id(group.getId()).build();
        ACommand aCommand = ACommand.builder().id(command.getId()).build();
        AChannelGroupCommand groupCommand = groupCommandRepository.findByCommandAndGroup(aCommand, aChannelGroup);
        if(groupCommand == null) {
            groupCommand = createCommandInGroupTo(command, group);
        }
        groupCommand.setEnabled(enabled);
        groupCommandRepository.save(groupCommand);
    }

    public AChannelGroupCommand createCommandInGroupTo(CommandDto command, ChannelGroupDto group) {
        AChannelGroupCommand channelGroupCommand = AChannelGroupCommand
                .builder()
                .command(ACommand.builder().id(command.getId()).build())
                .group(AChannelGroup.builder().id(group.getId()).build())
                .enabled(false)
                .build();

        groupCommandRepository.save(channelGroupCommand);
        return channelGroupCommand;
    }

    public AChannelGroupCommand getChannelGroupCommand(CommandDto command, ChannelGroupDto group) {
        ACommand aCommand = ACommand.builder().id(command.getId()).build();
        AChannelGroup aChannelGroup = AChannelGroup.builder().id(group.getId()).build();
        return groupCommandRepository.findByCommandAndGroup(aCommand, aChannelGroup);
    }

    public List<AChannelGroupCommand> getAllGroupCommandsForCommand(CommandDto command) {
        return groupCommandRepository.findByCommand(ACommand.builder().id(command.getId()).build());
    }


}
