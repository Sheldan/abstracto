package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CoolDownCheckResult;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.ServerIdChannelId;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Duration;

public interface CommandCoolDownService {
    /**
     * Acquires the lock which should be used when accessing the runtime storage
     */
    void takeLock();

    /**
     * Releases the lock which should be used then accessing the runtime storage
     */
    void releaseLock();
    CoolDownCheckResult allowedToExecuteCommand(Command command, CommandContext context);
    CoolDownCheckResult allowedToExecuteCommand(Command command, SlashCommandInteractionEvent slashCommandInteractionEvent);
    Duration getServerCoolDownForCommand(Command command, Long serverId);
    Duration getServerCoolDownForCommand(ACommand aCommand, Command command, Long serverId);
    Duration getChannelGroupCoolDownForCommand(Command command, ServerIdChannelId serverIdChannelId);
    Duration getChannelGroupCoolDownForCommand(ACommand aCommand, Command command, ServerIdChannelId serverIdChannelId);
    Duration getMemberCoolDownForCommand(Command command, ServerIdChannelId serverIdChannelId);
    Duration getMemberCoolDownForCommand(ACommand aCommand, Command command, ServerIdChannelId serverIdChannelId);
    void addServerCoolDown(Command command, Long serverId);
    void addServerCoolDown(Command command, Long serverId, boolean takeLock);
    void addChannelCoolDown(Command command, ServerIdChannelId context);
    void addChannelCoolDown(Command command, ServerIdChannelId context, boolean takeLock);
    void addMemberCoolDown(Command command, AServerChannelUserId context);
    void addMemberCoolDown(Command command, AServerChannelUserId context, boolean takeLock);
    void updateCoolDowns(Command command, CommandContext context);
    void updateCoolDowns(Command command, SlashCommandInteractionEvent event);
    void setCoolDownConfigForChannelGroup(AChannelGroup aChannelGroup, Duration groupCoolDown, Duration memberCoolDown);
    void clearCoolDownsForServer(Long serverId);
}
