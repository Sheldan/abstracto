package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.models.ACommand;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;

import java.util.List;

public interface ChannelGroupCommandManagementService {
    void setCommandInGroupTo(ACommand command, AChannelGroup group, Boolean enabled);
    AChannelGroupCommand createCommandInGroupTo(ACommand command, AChannelGroup group);
    AChannelGroupCommand getChannelGroupCommand(ACommand command, AChannelGroup group);
    List<AChannelGroupCommand> getAllGroupCommandsForCommand(ACommand command);
}
