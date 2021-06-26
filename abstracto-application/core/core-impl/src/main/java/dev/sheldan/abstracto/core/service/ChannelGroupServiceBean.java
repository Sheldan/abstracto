package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.exception.ChannelGroupNotFoundException;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.service.management.ChannelGroupCommandManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.exception.ChannelInMultipleChannelGroupsException;
import dev.sheldan.abstracto.core.exception.CommandInMultipleChannelGroupsException;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.provider.ChannelGroupInformation;
import dev.sheldan.abstracto.core.models.provider.ChannelGroupInformationRequest;
import dev.sheldan.abstracto.core.models.template.commands.ChannelGroupChannelModel;
import dev.sheldan.abstracto.core.models.template.commands.ChannelGroupModel;
import dev.sheldan.abstracto.core.provider.ChannelGroupInformationProvider;
import dev.sheldan.abstracto.core.service.management.ChannelGroupManagementService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChannelGroupServiceBean implements ChannelGroupService {

    @Autowired
    private ChannelGroupManagementService channelGroupManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Autowired
    private ChannelGroupCommandManagementService channelGroupCommandManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private List<ChannelGroupInformationProvider> channelGroupInformationProviders;

    @Override
    public AChannelGroup createChannelGroup(String name, Long serverId, ChannelGroupType channelGroupType) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return channelGroupManagementService.createChannelGroup(name, server, channelGroupType);
    }

    @Override
    public void deleteChannelGroup(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        channelGroupManagementService.deleteChannelGroup(name, server);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, TextChannel textChannel) {
        addChannelToChannelGroup(channelGroupName, textChannel.getIdLong(), textChannel.getGuild().getIdLong());
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, Long channelId, Long serverId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        addChannelToChannelGroup(channelGroupName, channel);
    }

    @Override
    public void addChannelToChannelGroup(String channelGroupName, AChannel channel) {
        AServer server = serverManagementService.loadOrCreate(channel.getServer().getId());
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        if(channelGroup == null) {
            throw new ChannelGroupNotFoundException(channelGroupName, channelGroupManagementService.getAllAvailableAsString(server));
        }
        if(!channelGroup.getChannelGroupType().getAllowsChannelsInMultiple()) {
            List<AChannelGroup> existingGroups = getChannelGroupsOfChannelWithType(channel, channelGroup.getChannelGroupType().getGroupTypeKey());
            if(!existingGroups.isEmpty()) {
                throw new ChannelInMultipleChannelGroupsException(existingGroups.get(0).getGroupName());
            }
        }
        channelGroupManagementService.addChannelToChannelGroup(channelGroup, channel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, TextChannel textChannel) {
        removeChannelFromChannelGroup(channelGroupName, textChannel.getIdLong(), textChannel.getGuild().getIdLong());
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, Long channelId, Long serverId) {
        AChannel channel = channelManagementService.loadChannel(channelId);
        removeChannelFromChannelGroup(channelGroupName, channel);
    }

    @Override
    public void removeChannelFromChannelGroup(String channelGroupName, AChannel channel) {
        AServer server = serverManagementService.loadOrCreate(channel.getServer().getId());
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        if(channelGroup == null) {
            throw new ChannelGroupNotFoundException(channelGroupName, channelGroupManagementService.getAllAvailableAsString(server));
        }
        channelGroupManagementService.removeChannelFromChannelGroup(channelGroup, channel);
    }

    @Override
    public void addCommandToChannelGroup(String commandName, String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        ACommand command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandNotFoundException();
        }
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        if(!channelGroup.getChannelGroupType().getAllowsCommandsInMultiple()) {
            List<AChannelGroupCommand> existingChannelGroupCommands = channelGroupCommandManagementService
                    .getAllGroupCommandsForCommandWithType(command, channelGroup.getChannelGroupType().getGroupTypeKey());
            if(!existingChannelGroupCommands.isEmpty()) {
                throw new CommandInMultipleChannelGroupsException(existingChannelGroupCommands.get(0).getGroup().getGroupName());
            }
        }
        channelGroupCommandManagementService.addCommandToGroup(command, channelGroup);
        log.info("Adding command {} to channel group {}.", command.getId(), channelGroup.getId());
    }

    @Override
    public void removeCommandFromChannelGroup(String commandName, String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        ACommand command = commandManagementService.findCommandByName(commandName);
        if(command == null) {
            throw new CommandNotFoundException();
        }
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        channelGroupCommandManagementService.removeCommandFromGroup(command, channelGroup);
        log.info("Removing command {} from channel group {}.", command.getId(), channelGroup.getId());
    }

    @Override
    public void disableChannelGroup(String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        log.info("Disabling channel group {} in server {}.", channelGroup.getId(), serverId);
        channelGroup.setEnabled(false);
    }

    @Override
    public void enableChannelGroup(String channelGroupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        AChannelGroup channelGroup = channelGroupManagementService.findByNameAndServer(channelGroupName, server);
        log.info("Enabling channel group {} in server {}.", channelGroup.getId(), serverId);
        channelGroup.setEnabled(true);
    }

    @Override
    public boolean isChannelInEnabledChannelGroupOfType(String channelGroupType, Long channelId) {
        return channelGroupManagementService.isChannelInEnabledChannelGroupOfType(channelGroupType, channelId);
    }

    @Override
    public boolean doesGroupExist(String groupName, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return channelGroupManagementService.findByNameAndServer(groupName, server) != null;
    }

    @Override
    public boolean isChannelInGroup(AChannel channel, AChannelGroup aChannelGroup) {
        return aChannelGroup.getChannels().contains(channel);
    }

    @Override
    public List<AChannelGroup> getChannelGroupsOfChannelWithType(AChannel channel, String groupTypeKey) {
        List<AChannelGroup> channelGroups = channelGroupManagementService.getAllChannelGroupsOfChannel(channel);
        return channelGroups
                .stream()
                .filter(aChannelGroup -> aChannelGroup.getChannelGroupType().getGroupTypeKey().equals(groupTypeKey))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChannelGroupModel> convertAChannelGroupToChannelGroupChannel(List<AChannelGroup> channelGroups) {
        List<ChannelGroupModel> converted = new ArrayList<>();
        channelGroups.forEach(group -> {
            List<ChannelGroupChannelModel> convertedChannels = new ArrayList<>();
            group.getChannels().forEach(channel -> {
                Optional<TextChannel> textChannelInGuild = channelService.getTextChannelFromServerOptional(channel.getServer().getId(), channel.getId());
                ChannelGroupChannelModel convertedChannel = ChannelGroupChannelModel
                        .builder()
                        .channel(channel)
                        .discordChannel(textChannelInGuild.orElse(null))
                        .build();
                convertedChannels.add(convertedChannel);
            });
            ChannelGroupModel channelGroup = ChannelGroupModel
                    .builder()
                    .name(group.getGroupName())
                    .typeKey(group.getChannelGroupType().getGroupTypeKey())
                    .channels(convertedChannels)
                    .enabled(group.getEnabled())
                    .channelGroupInformation(getAdditionalInformation(group))
                    .build();
            converted.add(channelGroup);
        });
        return converted;
    }

    private ChannelGroupInformation getAdditionalInformation(AChannelGroup channelGroup) {
        if(channelGroupInformationProviders == null) {
            return null;
        }
        ChannelGroupInformationRequest request = ChannelGroupInformationRequest
                .builder()
                .channelGroupId(channelGroup.getId())
                .channelGroupType(channelGroup.getChannelGroupType().getGroupTypeKey())
                .build();
        for (ChannelGroupInformationProvider provider : channelGroupInformationProviders) {
            if(provider.handlesRequest(request)) {
                return provider.retrieveInformation(request);
            }
        }
        return null;
    }
}
