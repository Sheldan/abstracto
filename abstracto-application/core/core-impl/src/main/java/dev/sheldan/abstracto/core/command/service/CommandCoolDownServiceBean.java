package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CoolDownCheckResult;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.CoolDownChannelGroup;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.models.ServerIdChannelId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import dev.sheldan.abstracto.core.service.ChannelGroupService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.CoolDownChannelGroupManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Component
@Slf4j
public class CommandCoolDownServiceBean implements CommandCoolDownService {

    @Autowired
    private CommandCoolDownRuntimeStorage storage;

    private static final Lock runTimeLock = new ReentrantLock();

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private CommandInServerManagementService commandInServerManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private ChannelGroupCommandManagementService channelGroupCommandService;

    @Autowired
    private CoolDownChannelGroupManagementService coolDownChannelGroupManagementService;

    public static final String COOL_DOWN_CHANNEL_GROUP_TYPE = "commandCoolDown";

    @Override
    public void takeLock() {
        runTimeLock.lock();
    }

    @Override
    public void releaseLock() {
        runTimeLock.unlock();
    }

    @Override
    public CoolDownCheckResult allowedToExecuteCommand(Command command, CommandContext context) {
        Long serverId = context.getGuild().getIdLong();
        Instant now = Instant.now();
        String commandName = command.getConfiguration().getName();
        Duration serverCooldown = null;
        Duration channelCooldown = null;
        Duration memberCooldown = null;
        if(storage.getServerCoolDowns().containsKey(serverId)) {
            CommandReUseMap serverMap = storage.getServerCoolDowns().get(serverId);
            Duration durationToExecuteIn = getDurationToExecuteIn(now, commandName, serverMap);
            if (durationIndicatesCoolDown(durationToExecuteIn)) {
                serverCooldown = durationToExecuteIn;
            }
        }
        if(storage.getChannelGroupCoolDowns().containsKey(serverId)) {
            Map<Long, CommandReUseMap> serverMap = storage.getChannelGroupCoolDowns().get(serverId);
            if(!serverMap.keySet().isEmpty()) {
                Long channelId = context.getChannel().getIdLong();
                AChannel channel = channelManagementService.loadChannel(channelId);
                List<AChannelGroup> channelGroups =
                        channelGroupService.getChannelGroupsOfChannelWithType(channel, COOL_DOWN_CHANNEL_GROUP_TYPE);
                for (AChannelGroup channelGroup : channelGroups) {
                    if(serverMap.containsKey(channelGroup.getId())) {
                        CommandReUseMap channelGroupMap = serverMap.get(channelGroup.getId());
                        Duration durationToExecuteIn = getDurationToExecuteIn(now, commandName, channelGroupMap);
                        if (durationIndicatesCoolDown(durationToExecuteIn)) {
                            channelCooldown = durationToExecuteIn;
                        }
                    }
                }
            }
        }
        if(storage.getMemberCoolDowns().containsKey(serverId)) {
            Map<Long, CommandReUseMap> serverMap = storage.getMemberCoolDowns().get(serverId);
            if(!serverMap.keySet().isEmpty()) {
                Long memberId = context.getAuthor().getIdLong();
                if(serverMap.containsKey(memberId)) {
                    CommandReUseMap commandReUseMap = serverMap.get(memberId);
                    Duration durationToExecuteIn = getDurationToExecuteIn(now, commandName, commandReUseMap);
                    if (durationIndicatesCoolDown(durationToExecuteIn)) {
                        memberCooldown = durationToExecuteIn;
                    }
                }
            }
        }
        if(serverCooldown != null || channelCooldown != null || memberCooldown != null) {
            Long serverSeconds = serverCooldown != null ? serverCooldown.getSeconds() : 0L;
            Long channelSeconds = channelCooldown != null ? channelCooldown.getSeconds() : 0L;
            Long memberSeconds = memberCooldown != null ? memberCooldown.getSeconds() : 0L;
            if(serverSeconds > channelSeconds && serverSeconds > memberSeconds) {
                return CoolDownCheckResult.getServerCoolDown(serverCooldown);
            }
            if(channelSeconds > serverSeconds && channelSeconds > memberSeconds) {
                return CoolDownCheckResult.getChannelGroupCoolDown(channelCooldown);
            }
            return CoolDownCheckResult.getMemberCoolDown(memberCooldown);
        }
        return CoolDownCheckResult.noCoolDown();
    }

    private boolean durationIndicatesCoolDown(Duration duration) {
        return !duration.equals(Duration.ZERO) && !duration.isNegative();
    }

    @Override
    public Duration getServerCoolDownForCommand(Command command, Long serverId) {
        CommandConfiguration commandConfiguration = command.getConfiguration();
        ACommand aCommand = commandManagementService.findCommandByName(commandConfiguration.getName());
        return getServerCoolDownForCommand(aCommand, command, serverId);
    }

    @Override
    public Duration getServerCoolDownForCommand(ACommand aCommand, Command command, Long serverId) {
        CommandConfiguration commandConfiguration = command.getConfiguration();
        ACommandInAServer commandInServer = commandInServerManagementService.getCommandForServer(aCommand, serverId);
        if(commandInServer.getCoolDown() != null) {
            return Duration.ofSeconds(commandInServer.getCoolDown());
        }
        if(commandConfiguration.getCoolDownConfig() != null) {
            return commandConfiguration.getCoolDownConfig().getServerCoolDown();
        }
        return Duration.ZERO;
    }

    @Override
    public Duration getChannelGroupCoolDownForCommand(Command command, ServerIdChannelId serverIdChannelId) {
        CommandConfiguration commandConfiguration = command.getConfiguration();
        ACommand aCommand = commandManagementService.findCommandByName(commandConfiguration.getName());
        return getChannelGroupCoolDownForCommand(aCommand, command, serverIdChannelId);
    }

    @Override
    public Duration getChannelGroupCoolDownForCommand(ACommand aCommand, Command command, ServerIdChannelId serverIdChannelId) {
        CommandConfiguration commandConfiguration = command.getConfiguration();
        AChannel channel = channelManagementService.loadChannel(serverIdChannelId.getChannelId());
        List<AChannelGroup> channelGroups = channelGroupService.getChannelGroupsOfChannelWithType(channel, COOL_DOWN_CHANNEL_GROUP_TYPE);
        List<AChannelGroupCommand> allChannelGroupsOfCommand =
                channelGroupCommandService.getAllGroupCommandsForCommandInGroups(aCommand, channelGroups);
        if(!allChannelGroupsOfCommand.isEmpty()) {
            Long durationInSeconds = 0L;
            if(allChannelGroupsOfCommand.size() > 1) {
                log.info("Found multiple channel groups of type commandCoolDown for command {} in server {}. ",
                        command.getConfiguration().getName(), serverIdChannelId.getServerId());
            }
            for (AChannelGroupCommand channelGroupCommand : allChannelGroupsOfCommand) {
                CoolDownChannelGroup channelGroup = coolDownChannelGroupManagementService.findByChannelGroupId(channelGroupCommand.getGroup().getId());
                if (channelGroup.getChannelCoolDown() != null) {
                    durationInSeconds = Math.max(durationInSeconds, channelGroup.getChannelCoolDown());
                }
            }
            return Duration.ofSeconds(durationInSeconds);
        }
        if(commandConfiguration.getCoolDownConfig() != null) {
            return commandConfiguration.getCoolDownConfig().getChannelCoolDown();
        }
        return Duration.ZERO;
    }

    @Override
    public Duration getMemberCoolDownForCommand(Command command, ServerIdChannelId serverIdChannelId) {
        CommandConfiguration commandConfiguration = command.getConfiguration();
        ACommand aCommand = commandManagementService.findCommandByName(commandConfiguration.getName());
        return getMemberCoolDownForCommand(aCommand, command, serverIdChannelId);
    }

    @Override
    public Duration getMemberCoolDownForCommand(ACommand aCommand, Command command, ServerIdChannelId serverIdChannelId) {
        CommandConfiguration commandConfiguration = command.getConfiguration();
        AChannel channel = channelManagementService.loadChannel(serverIdChannelId.getChannelId());
        List<AChannelGroup> channelGroups = channelGroupService.getChannelGroupsOfChannelWithType(channel, COOL_DOWN_CHANNEL_GROUP_TYPE);
        List<AChannelGroupCommand> allChannelGroupsOfCommand =
                channelGroupCommandService.getAllGroupCommandsForCommandInGroups(aCommand, channelGroups);
        if(!allChannelGroupsOfCommand.isEmpty()) {
            Long durationInSeconds = 0L;
            if(allChannelGroupsOfCommand.size() > 1) {
                log.info("Found multiple channel groups of type commandCoolDown for command {} in server {}. ",
                        command.getConfiguration().getName(), serverIdChannelId.getServerId());
            }
            for (AChannelGroupCommand channelGroupCommand : allChannelGroupsOfCommand) {
                CoolDownChannelGroup channelGroup = coolDownChannelGroupManagementService.findByChannelGroupId(channelGroupCommand.getGroup().getId());
                if (channelGroup.getMemberCoolDown() != null) {
                    durationInSeconds = Math.max(durationInSeconds, channelGroup.getMemberCoolDown());
                }
            }
            return Duration.ofSeconds(durationInSeconds);
        }
        if(commandConfiguration.getCoolDownConfig() != null) {
            return commandConfiguration.getCoolDownConfig().getMemberCoolDown();
        }
        return Duration.ZERO;
    }

    @Override
    public void addServerCoolDown(Command command, Long serverId) {
       addServerCoolDown(command, serverId, true);
    }

    @Override
    public void addServerCoolDown(Command command, Long serverId, boolean takeLock) {
        if(takeLock) {
            takeLock();
        }
        try {
            Duration coolDown = getServerCoolDownForCommand(command, serverId);
            if(coolDown.equals(Duration.ZERO)) {
                return;
            }
            Instant newExecutionPoint = Instant.now().plus(coolDown);
            String commandName = command.getConfiguration().getName();
            Map<Long, CommandReUseMap> serverCoolDowns = storage.getServerCoolDowns();
            createReUseMapIfNotExists(newExecutionPoint, commandName, serverCoolDowns, serverId);
        } finally {
            if(takeLock) {
                releaseLock();
            }
        }
    }

    @Override
    public void addChannelCoolDown(Command command, ServerIdChannelId serverIdChannelId) {
       addChannelCoolDown(command, serverIdChannelId, true);
    }

    @Override
    public void addChannelCoolDown(Command command, ServerIdChannelId serverIdChannelId, boolean takeLock) {
        if(takeLock) {
            takeLock();
        }
        try {
            Duration coolDown = getChannelGroupCoolDownForCommand(command, serverIdChannelId);
            if(coolDown.equals(Duration.ZERO)) {
                return;
            }
            Instant newExecutionPoint = Instant.now().plus(coolDown);
            String commandName = command.getConfiguration().getName();
            Long serverId = serverIdChannelId.getServerId();
            Map<Long, Map<Long, CommandReUseMap>> serverChannelGroupCoolDowns = storage.getChannelGroupCoolDowns();
            Map<Long, CommandReUseMap> channelGroupCoolDowns;
            if(serverChannelGroupCoolDowns.containsKey(serverId)) {
                channelGroupCoolDowns = serverChannelGroupCoolDowns.get(serverId);
            } else {
                channelGroupCoolDowns = new HashMap<>();
                serverChannelGroupCoolDowns.put(serverId, channelGroupCoolDowns);
            }

            ACommand aCommand = commandManagementService.findCommandByName(commandName);
            Long channelId = serverIdChannelId.getChannelId();
            AChannel channel = channelManagementService.loadChannel(channelId);
            List<AChannelGroup> channelGroups = channelGroupService.getChannelGroupsOfChannelWithType(channel, COOL_DOWN_CHANNEL_GROUP_TYPE);
            List<AChannelGroupCommand> allChannelGroupsOfCommand =
                    channelGroupCommandService.getAllGroupCommandsForCommandInGroups(aCommand, channelGroups);
            if (!allChannelGroupsOfCommand.isEmpty()) {
                AChannelGroupCommand groupCommand = allChannelGroupsOfCommand.get(0);
                if (allChannelGroupsOfCommand.size() > 1) {
                    log.info("Found multiple channel groups of type commandCoolDown for command {} in server {}. Taking the command group {}.",
                            command.getConfiguration().getName(), serverId, groupCommand.getCommandInGroupId());
                }
                Long channelGroupId = groupCommand.getGroup().getId();
                if (channelGroupCoolDowns.containsKey(channelGroupId)) {
                    createReUseMapIfNotExists(newExecutionPoint, commandName, channelGroupCoolDowns, channelGroupId);
                } else {
                    CommandReUseMap commandReUseMap = createCommandReUseMap(newExecutionPoint, commandName);
                    channelGroupCoolDowns.put(channelGroupId, commandReUseMap);
                }

            } else {
                log.debug("Not adding a cool down on channel {} in server {}, because there is not channel group configured.",
                        channelId, serverId);
            }
        } finally {
            if(takeLock) {
                releaseLock();
            }
        }
    }

    private void createReUseMapIfNotExists(Instant newExecutionPoint, String commandName, Map<Long, CommandReUseMap> reuseMapMap, Long mapId) {
        if (reuseMapMap.containsKey(mapId)) {
            Map<String, Instant> reUseTimes = reuseMapMap.get(mapId).getReUseTimes();
            reUseTimes.put(commandName, newExecutionPoint);
        } else {
            CommandReUseMap commandReUseMap = createCommandReUseMap(newExecutionPoint, commandName);
            reuseMapMap.put(mapId, commandReUseMap);
        }
    }

    @Override
    public void addMemberCoolDown(Command command, AServerChannelUserId serverChannelUserId) {
        addMemberCoolDown(command, serverChannelUserId, true);
    }

    @Override
    public void addMemberCoolDown(Command command, AServerChannelUserId serverChannelUserId, boolean takeLock) {
        if(takeLock) {
            takeLock();
        }
        try {
            Duration coolDown = getMemberCoolDownForCommand(command, serverChannelUserId.toServerChannelId());
            if(coolDown.equals(Duration.ZERO)) {
                return;
            }
            Long serverId = serverChannelUserId.getGuildId();
            Instant newExecutionPoint = Instant.now().plus(coolDown);
            String commandName = command.getConfiguration().getName();
            Map<Long, Map<Long, CommandReUseMap>> serverMemberCoolDowns = storage.getMemberCoolDowns();
            Map<Long, CommandReUseMap> memberCoolDowns;
            if(serverMemberCoolDowns.containsKey(serverId)) {
                memberCoolDowns = serverMemberCoolDowns.get(serverId);
            } else {
                memberCoolDowns = new HashMap<>();
                serverMemberCoolDowns.put(serverId, memberCoolDowns);
            }
            Long userId = serverChannelUserId.getUserId();
            if (memberCoolDowns.containsKey(userId)) {
                createReUseMapIfNotExists(newExecutionPoint, commandName, memberCoolDowns, userId);
            } else {
                CommandReUseMap commandReUseMap = createCommandReUseMap(newExecutionPoint, commandName);
                memberCoolDowns.put(userId, commandReUseMap);
            }
        } finally {
            if(takeLock) {
                releaseLock();
            }
        }
    }

    @Override
    public void updateCoolDowns(Command command, CommandContext context) {
        takeLock();
        try {
            AServerChannelUserId contextIds = AServerChannelUserId
                    .builder()
                    .channelId(context.getChannel().getIdLong())
                    .userId(context.getAuthor().getIdLong())
                    .guildId(context.getGuild().getIdLong())
                    .build();
            addServerCoolDown(command, contextIds.getGuildId(), false);
            addChannelCoolDown(command, contextIds.toServerChannelId(), false);
            addMemberCoolDown(command, contextIds, false);
        } finally {
            releaseLock();
        }
    }

    @Override
    public void setCoolDownConfigForChannelGroup(AChannelGroup aChannelGroup, Duration groupCoolDown, Duration memberCoolDown) {
        CoolDownChannelGroup cdChannelGroup = coolDownChannelGroupManagementService.findByChannelGroupId(aChannelGroup.getId());
        cdChannelGroup.setChannelCoolDown(groupCoolDown.getSeconds());
        cdChannelGroup.setMemberCoolDown(memberCoolDown.getSeconds());
    }

    @Override
    public void clearCoolDownsForServer(Long serverId) {
        takeLock();
         try {
             storage.getServerCoolDowns().remove(serverId);
             storage.getChannelGroupCoolDowns().remove(serverId);
             storage.getMemberCoolDowns().remove(serverId);
         } finally {
             releaseLock();
         }
    }

    private Duration getDurationToExecuteIn(Instant now, String commandName, CommandReUseMap reuseMap) {
        if(reuseMap.getReUseTimes().containsKey(commandName)) {
            Instant reUseTime = reuseMap.getReUseTimes().get(commandName);
            return Duration.between(now, reUseTime);
        }
        return Duration.ZERO;
    }

    private CommandReUseMap createCommandReUseMap(Instant newExecutionPoint, String commandName) {
        Map<String, Instant> reUseTimes = new HashMap<>();
        reUseTimes.put(commandName, newExecutionPoint);
        return CommandReUseMap.builder().reUseTimes(reUseTimes).build();
    }

    @Transactional
    public void cleanUpCooldownStorage() {
        takeLock();
        try {
            cleanUpLongReUseMap(storage.getServerCoolDowns());
            cleanUpLongLongReUseMap(storage.getMemberCoolDowns());
            cleanUpLongLongReUseMap(storage.getChannelGroupCoolDowns());
        } finally {
            releaseLock();
        }
    }

    private void cleanUpLongLongReUseMap(Map<Long, Map<Long, CommandReUseMap>> longLongReuseMap) {
        longLongReuseMap.forEach((aLong, longCommandReUseMapMap) -> cleanUpLongReUseMap(longCommandReUseMapMap));
        longLongReuseMap.entrySet().removeIf(longMapEntry -> longMapEntry.getValue().isEmpty());
    }

    private void cleanUpLongReUseMap(Map<Long, CommandReUseMap> map) {
        map.forEach((aLong, commandReUseMap) -> cleanUpReUseMap(commandReUseMap));
        map.entrySet().removeIf(longCommandReUseMapEntry -> longCommandReUseMapEntry.getValue().getReUseTimes().isEmpty());
    }

    private void cleanUpReUseMap(CommandReUseMap commandReUseMap) {
        List<String> commandsToRemove = new ArrayList<>();
        Instant now = Instant.now();
        commandReUseMap.getReUseTimes().forEach((commandName, reUseTime) -> {
            if(reUseTime.isBefore(now)) {
                commandsToRemove.add(commandName);
            }
        });
        log.debug("Deleting {} command mappings.", commandsToRemove.size());
        commandsToRemove.forEach(commandName -> commandReUseMap.getReUseTimes().remove(commandName));
    }
}
