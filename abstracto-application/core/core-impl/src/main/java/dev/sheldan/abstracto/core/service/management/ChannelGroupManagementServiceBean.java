package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.exception.ChannelAlreadyInChannelGroupException;
import dev.sheldan.abstracto.core.command.exception.ChannelGroupExistsException;
import dev.sheldan.abstracto.core.command.exception.ChannelGroupNotFoundException;
import dev.sheldan.abstracto.core.command.exception.ChannelNotInChannelGroupException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.ChannelGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChannelGroupManagementServiceBean implements ChannelGroupManagementService {

    @Autowired
    private ChannelGroupRepository channelGroupRepository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public AChannelGroup createChannelGroup(String name, AServer server) {
        name = name.toLowerCase();
        if(doesChannelGroupExist(name, server)) {
            throw new ChannelGroupExistsException(name);
        }
        AChannelGroup channelGroup = AChannelGroup
                .builder()
                .groupName(name)
                .server(server)
                .build();
        log.info("Creating new channel group in server {}.", server.getId());
        channelGroupRepository.save(channelGroup);
        return channelGroup;
    }

    @Override
    public boolean doesChannelGroupExist(String name, AServer server) {
        return channelGroupRepository.existsByGroupNameAndServer(name, server);
    }

    @Override
    public void deleteChannelGroup(String name, AServer server) {
        name = name.toLowerCase();
        AChannelGroup existing = findByNameAndServer(name, server);
        if(existing == null) {
            throw new ChannelGroupNotFoundException(name, getAllAvailableAsString(server));
        }
        log.info("Deleting channel group {} in server {}.", existing.getId(), server.getId());
        channelGroupRepository.delete(existing);
    }

    @Override
    public AChannelGroup addChannelToChannelGroup(AChannelGroup channelGroup, AChannel channel) {
        Predicate<AChannel> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup.getChannels().stream().anyMatch(channelInGroupPredicate)) {
            throw new ChannelAlreadyInChannelGroupException(channel, channelGroup);
        }
        channelGroup.getChannels().add(channel);
        channel.getGroups().add(channelGroup);
        log.info("Adding channel {} to channel group {} in server {}.", channel.getId(), channelGroup.getId(), channel.getServer().getId());
        return channelGroup;
    }

    @Override
    public void removeChannelFromChannelGroup(AChannelGroup channelGroup, AChannel channel) {
        Predicate<AChannel> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup.getChannels().stream().noneMatch(channelInGroupPredicate)) {
            throw new ChannelNotInChannelGroupException(channel, channelGroup);
        }
        channelGroup.getChannels().removeIf(channelInGroupPredicate);
        channel.getGroups().removeIf(channelGroup1 -> channelGroup1.getId().equals(channelGroup.getId()));
        log.info("Removing channel {} from channel group {} in server {}.", channel.getId(), channelGroup.getId(), channel.getServer().getId());
    }

    @Override
    public AChannelGroup findByNameAndServer(String name, AServer server) {
        name = name.toLowerCase();
        return channelGroupRepository.findByGroupNameAndServer(name, server);
    }

    @Override
    public List<AChannelGroup> findAllInServer(AServer server) {
        return channelGroupRepository.findByServer(server);
    }

    @Override
    public List<String> getAllAvailableAsString(AServer server) {
        return findAllInServer(server).stream().map(AChannelGroup::getGroupName).collect(Collectors.toList());
    }

    @Override
    public List<AChannelGroup> findAllInServer(Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return findAllInServer(server);
    }
}
