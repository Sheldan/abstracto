package dev.sheldan.abstracto.core.command.service;

public interface CommandDisabledService {
    void disableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId);
    void enableCommandInChannelGroup(String commandName, String channelGroupName, Long serverId);
}
