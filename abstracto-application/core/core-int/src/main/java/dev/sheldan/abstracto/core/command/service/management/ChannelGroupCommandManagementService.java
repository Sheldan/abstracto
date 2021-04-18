package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;

import java.util.List;

public interface ChannelGroupCommandManagementService {
    void setCommandInGroupTo(ACommand command, AChannelGroup group, Boolean enabled);
    void addCommandToGroup(ACommand command, AChannelGroup group);
    void removeCommandFromGroup(ACommand command, AChannelGroup group);
    AChannelGroupCommand createCommandInGroup(ACommand command, AChannelGroup group);
    AChannelGroupCommand getChannelGroupCommand(ACommand command, AChannelGroup group);
    List<AChannelGroupCommand> getAllGroupCommandsForCommand(ACommand command);
    List<AChannelGroupCommand> getAllGroupCommandsForCommandInGroups(ACommand command, List<AChannelGroup> groups);
    List<AChannelGroupCommand> getAllGroupCommandsForCommandWithType(ACommand command, String channelGroupType);
}
