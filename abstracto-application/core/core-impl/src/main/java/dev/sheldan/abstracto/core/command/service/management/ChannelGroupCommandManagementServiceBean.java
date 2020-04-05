package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.ACommand;
import dev.sheldan.abstracto.core.command.repository.ChannelGroupCommandRepository;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChannelGroupCommandManagementServiceBean implements ChannelGroupCommandManagementService {

    @Autowired
    private ChannelGroupCommandRepository groupCommandRepository;

    @Override
    public void setCommandInGroupTo(ACommand command, AChannelGroup group, Boolean enabled) {
        AChannelGroupCommand groupCommand = groupCommandRepository.findByCommandAndGroup(command, group);
        if(groupCommand == null) {
            groupCommand = createCommandInGroupTo(command, group);
        }
        groupCommand.setEnabled(enabled);
        groupCommandRepository.save(groupCommand);
    }

    @Override
    public AChannelGroupCommand createCommandInGroupTo(ACommand command, AChannelGroup group) {
        AChannelGroupCommand channelGroupCommand = AChannelGroupCommand
                .builder()
                .command(command)
                .group(group)
                .enabled(false)
                .build();

        groupCommandRepository.save(channelGroupCommand);
        return channelGroupCommand;
    }

    @Override
    public AChannelGroupCommand getChannelGroupCommand(ACommand command, AChannelGroup group) {
        return groupCommandRepository.findByCommandAndGroup(command, group);
    }

    @Override
    public List<AChannelGroupCommand> getAllGroupCommandsForCommand(ACommand command) {
        return groupCommandRepository.findByCommand(command);
    }


}
