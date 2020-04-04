package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.ChannelException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.repository.ChannelGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class ChannelGroupManagementServiceBean implements ChannelGroupManagementService {

    @Autowired
    private ChannelGroupRepository channelGroupRepository;

    @Override
    public AChannelGroup createChannelGroup(String name) {
        AChannelGroup channelGroup = AChannelGroup
                .builder()
                .groupName(name)
                .build();
        channelGroupRepository.save(channelGroup);
        return channelGroup;
    }

    @Override
    public AChannelGroup addChannelToChannelGroup(AChannelGroup channelGroup, AChannel channel) {
        Predicate<AChannel> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup.getChannels().stream().anyMatch(channelInGroupPredicate)) {
            throw new ChannelException(String.format("Channel %s is already part of group %s.", channel.getId(), channelGroup.getGroupName()));
        }
        channelGroup.getChannels().add(channel);
        channel.getGroups().add(channelGroup);
        channelGroupRepository.save(channelGroup);
        return channelGroup;
    }

    @Override
    public void removeChannelFromChannelGroup(AChannelGroup channelGroup, AChannel channel) {
        Predicate<AChannel> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup.getChannels().stream().noneMatch(channelInGroupPredicate)) {
            throw new ChannelException(String.format("Channel %s is not part of group %s.", channel.getId(), channelGroup.getGroupName()));
        }
        channelGroup.getChannels().removeIf(channelInGroupPredicate);
        channel.getGroups().removeIf(channelGroup1 -> channelGroup1.getId().equals(channelGroup.getId()));
        channelGroupRepository.save(channelGroup);
    }

    @Override
    public AChannelGroup findByName(String name) {
        return channelGroupRepository.findByGroupName(name);
    }
}
