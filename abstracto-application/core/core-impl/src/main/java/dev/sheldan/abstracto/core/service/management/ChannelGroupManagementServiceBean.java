package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.exception.ChannelGroupException;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.repository.ChannelGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class ChannelGroupManagementServiceBean implements ChannelGroupManagementService {

    @Autowired
    private ChannelGroupRepository channelGroupRepository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public AChannelGroup createChannelGroup(String name, AServer server) {
        name = name.toLowerCase();
        if(doesChannelGroupExist(name, server)) {
            throw new ChannelGroupException("Channel group already exists.");
        }
        AChannelGroup channelGroup = AChannelGroup
                .builder()
                .groupName(name)
                .server(server)
                .build();
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
            throw new ChannelGroupException(String.format("Channel group %s does not exist", name));
        }
        channelGroupRepository.delete(existing);
    }

    @Override
    public AChannelGroup addChannelToChannelGroup(AChannelGroup channelGroup, AChannel channel) {
        Predicate<AChannel> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup == null) {
            throw new ChannelGroupException("Channel group was not found.");
        }
        if(channelGroup.getChannels().stream().anyMatch(channelInGroupPredicate)) {
            throw new ChannelGroupException(String.format("Channel %s is already part of group %s.", channel.getId(), channelGroup.getGroupName()));
        }
        channelGroup.getChannels().add(channel);
        channel.getGroups().add(channelGroup);
        return channelGroup;
    }

    @Override
    public void removeChannelFromChannelGroup(AChannelGroup channelGroup, AChannel channel) {
        Predicate<AChannel> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup.getChannels().stream().noneMatch(channelInGroupPredicate)) {
            throw new ChannelGroupException(String.format("Channel %s is not part of group %s.", channel.getId(), channelGroup.getGroupName()));
        }
        channelGroup.getChannels().removeIf(channelInGroupPredicate);
        channel.getGroups().removeIf(channelGroup1 -> channelGroup1.getId().equals(channelGroup.getId()));
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
    public List<AChannelGroup> findAllInServer(Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return findAllInServer(server);
    }
}
