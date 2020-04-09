package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.exception.ChannelGroupException;
import dev.sheldan.abstracto.core.exception.ChannelException;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AChannelGroup;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.core.models.converter.ChannelGroupConverter;
import dev.sheldan.abstracto.core.models.converter.ServerConverter;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ChannelGroupDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.repository.ChannelGroupRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class ChannelGroupManagementServiceBean {

    @Autowired
    private ChannelGroupRepository channelGroupRepository;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    @Autowired
    private ServerConverter serverConverter;

    @Autowired
    private ChannelGroupConverter channelGroupConverter;

    @Autowired
    private ChannelConverter channelConverter;

    public AChannelGroup createChannelGroup(String name, ServerDto server) {
        name = name.toLowerCase();
        AChannelGroup channelGroup = AChannelGroup
                .builder()
                .groupName(name)
                .server(serverConverter.fromDto(server))
                .build();
        channelGroupRepository.save(channelGroup);
        return channelGroup;
    }

    public void deleteChannelGroup(String name, ServerDto server) {
        name = name.toLowerCase();
        AChannelGroup existing = findByNameAndServer(name, serverConverter.fromDto(server));
        if(existing == null) {
            throw new ChannelGroupException(String.format("Channel group %s does not exist", name));
        }
        channelGroupRepository.delete(existing);
    }

    public ChannelGroupDto addChannelToChannelGroup(ChannelGroupDto channelGroup, ChannelDto channel) {
        Predicate<ChannelDto> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup == null) {
            throw new ChannelGroupException("Channel group was not found.");
        }
        if(channelGroup.getChannels().stream().anyMatch(channelInGroupPredicate)) {
            throw new ChannelException(String.format("Channel %s is already part of group %s.", channel.getId(), channelGroup.getGroupName()));
        }
        channelGroup.getChannels().add(channel);
        AChannel channel1 = channelConverter.fromDto(channel);
        AChannelGroup entity = channelGroupConverter.fromChannelGroup(channelGroup);
        channel1.getGroups().add(entity);
        channelGroupRepository.save(entity);
        return channelGroup;
    }

    public void removeChannelFromChannelGroup(ChannelGroupDto channelGroup, ChannelDto channel) {
        Predicate<ChannelDto> channelInGroupPredicate = channel1 -> channel1.getId().equals(channel.getId());
        if(channelGroup.getChannels().stream().noneMatch(channelInGroupPredicate)) {
            throw new ChannelException(String.format("Channel %s is not part of group %s.", channel.getId(), channelGroup.getGroupName()));
        }
        channelGroup.getChannels().removeIf(channelInGroupPredicate);
        channelGroupRepository.save(channelGroupConverter.fromChannelGroup(channelGroup));
    }

    public ChannelGroupDto findByNameAndServer(String name, ServerDto server) {
        name = name.toLowerCase();
        AChannelGroup byGroupNameAndServer = channelGroupRepository.findByGroupNameAndServer(name, serverConverter.fromDto(server));
        return channelGroupConverter.fromChannelGroup(byGroupNameAndServer);
    }

    private AChannelGroup findByNameAndServer(String name, AServer server) {
        name = name.toLowerCase();
        return channelGroupRepository.findByGroupNameAndServer(name, server);
    }

    public List<AChannelGroup> findAllInServer(ServerDto server) {
        return findAllInServer(serverConverter.fromDto(server));
    }

    private List<AChannelGroup> findAllInServer(AServer server) {
        return channelGroupRepository.findByServer(server);
    }

    public List<AChannelGroup> findAllInServer(Long serverId) {
        ServerDto server = serverManagementService.loadOrCreate(serverId);
        return findAllInServer(server);
    }
}
