package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.repository.ChannelGroupCommandRepository;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ChannelGroupCommandManagementServiceBean implements ChannelGroupCommandManagementService {

    @Autowired
    private ChannelGroupCommandRepository groupCommandRepository;

    @Override
    public void setCommandInGroupTo(ACommand command, AChannelGroup group, Boolean enabled) {
        Optional<AChannelGroupCommand> groupCommandOptional = groupCommandRepository.findByCommandAndGroup(command, group);
        AChannelGroupCommand groupCommand = groupCommandOptional.orElseGet(() -> createCommandInGroup(command, group));

        groupCommand.setEnabled(enabled);
        log.trace("Setting command {} enabled in group {} to {}.", command.getName(), group.getId(), enabled);
        groupCommandRepository.save(groupCommand);
    }

    @Override
    public AChannelGroupCommand createCommandInGroup(ACommand command, AChannelGroup group) {
        AChannelGroupCommand channelGroupCommand = AChannelGroupCommand
                .builder()
                .command(command)
                .server(group.getServer())
                .group(group)
                .enabled(false)
                .build();

        log.info("Creating command {} in group {}.", command.getName(), group.getId());

        return groupCommandRepository.save(channelGroupCommand);
    }

    @Override
    public AChannelGroupCommand getChannelGroupCommand(ACommand command, AChannelGroup group) {
        return groupCommandRepository.findByCommandAndGroup(command, group).orElseThrow(() -> new AbstractoRunTimeException("Command not found in group."));
    }

    @Override
    public List<AChannelGroupCommand> getAllGroupCommandsForCommand(ACommand command) {
        return groupCommandRepository.findByCommand(command);
    }


}
