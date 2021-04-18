package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.exception.CommandNotFoundInGroupException;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.repository.ChannelGroupCommandRepository;
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
        log.debug("Setting command {} enabled in group {} to {}.", command.getName(), group.getId(), enabled);
    }

    @Override
    public void addCommandToGroup(ACommand command, AChannelGroup group) {
        Optional<AChannelGroupCommand> groupCommandOptional = groupCommandRepository.findByCommandAndGroup(command, group);
        if(!groupCommandOptional.isPresent()) {
            createCommandInGroup(command, group);
        }
    }

    @Override
    public void removeCommandFromGroup(ACommand command, AChannelGroup group) {
        Optional<AChannelGroupCommand> groupCommandOptional = groupCommandRepository.findByCommandAndGroup(command, group);
        if(!groupCommandOptional.isPresent()) {
            throw new CommandNotFoundInGroupException();
        }
        groupCommandOptional.ifPresent(channelGroupCommand -> groupCommandRepository.delete(channelGroupCommand));
    }

    @Override
    public AChannelGroupCommand createCommandInGroup(ACommand command, AChannelGroup group) {
        AChannelGroupCommand channelGroupCommand = AChannelGroupCommand
                .builder()
                .command(command)
                .server(group.getServer())
                .group(group)
                .enabled(true)
                .build();

        log.info("Creating command {} in group {}.", command.getName(), group.getId());

        return groupCommandRepository.save(channelGroupCommand);
    }

    @Override
    public AChannelGroupCommand getChannelGroupCommand(ACommand command, AChannelGroup group) {
        return groupCommandRepository.findByCommandAndGroup(command, group).orElseThrow(CommandNotFoundInGroupException::new);
    }

    @Override
    public List<AChannelGroupCommand> getAllGroupCommandsForCommand(ACommand command) {
        return groupCommandRepository.findByCommand(command);
    }

    @Override
    public List<AChannelGroupCommand> getAllGroupCommandsForCommandInGroups(ACommand command, List<AChannelGroup> groups) {
        return groupCommandRepository.findByCommandAndGroupIn(command, groups);
    }

    @Override
    public List<AChannelGroupCommand> getAllGroupCommandsForCommandWithType(ACommand command, String channelGroupType) {
        return groupCommandRepository.findByCommandAndGroup_ChannelGroupType_GroupTypeKey(command, channelGroupType);
    }

}
