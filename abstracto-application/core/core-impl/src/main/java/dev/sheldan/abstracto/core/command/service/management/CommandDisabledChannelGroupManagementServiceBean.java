package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.CommandDisabledChannelGroup;
import dev.sheldan.abstracto.core.command.repository.CommandDisabledChannelGroupRepository;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.service.management.CommandDisabledChannelGroupManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommandDisabledChannelGroupManagementServiceBean implements CommandDisabledChannelGroupManagementService {

    @Autowired
    private CommandDisabledChannelGroupRepository commandDisabledChannelGroupRepository;

    @Override
    public CommandDisabledChannelGroup createCommandDisabledChannelGroup(AChannelGroup channelGroup) {
        CommandDisabledChannelGroup commandDisabledChannelGroup = CommandDisabledChannelGroup
                .builder()
                .channelGroup(channelGroup)
                .id(channelGroup.getId())
                .build();
        log.info("Creating command disabled channel group for channel group {} in server {}.", channelGroup.getId(), channelGroup.getServer().getId());
        return commandDisabledChannelGroupRepository.save(commandDisabledChannelGroup);
    }

    @Override
    public void deleteCommandDisabledChannelGroup(AChannelGroup channelGroup) {
        log.info("Deleting command disabled channel group for channel group {} in server {}.", channelGroup.getId(), channelGroup.getServer().getId());
        commandDisabledChannelGroupRepository.deleteById(channelGroup.getId());
    }

    @Override
    public CommandDisabledChannelGroup findViaChannelGroup(AChannelGroup channelGroup) {
        return commandDisabledChannelGroupRepository.getReferenceById(channelGroup.getId());
    }
}
