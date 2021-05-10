package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.exception.*;
import dev.sheldan.abstracto.core.listener.async.entity.AsyncChannelGroupCreatedListenerManager;
import dev.sheldan.abstracto.core.listener.sync.entity.SyncChannelGroupDeletedListenerManager;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ChannelGroupType;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupCreatedListenerModel;
import dev.sheldan.abstracto.core.models.listener.ChannelGroupDeletedListenerModel;
import dev.sheldan.abstracto.core.repository.ChannelGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChannelGroupManagementServiceBean implements ChannelGroupManagementService {

    @Autowired
    private ChannelGroupRepository channelGroupRepository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SyncChannelGroupDeletedListenerManager deletedListenerManager;

    @Autowired
    private AsyncChannelGroupCreatedListenerManager createdListenerManager;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public AChannelGroup createChannelGroup(String name, AServer server, ChannelGroupType channelGroupType) {
        if(doesChannelGroupExist(name, server)) {
            throw new ChannelGroupExistsException(name);
        }
        AChannelGroup channelGroup = AChannelGroup
                .builder()
                .groupName(name)
                .channelGroupType(channelGroupType)
                .server(server)
                .enabled(true)
                .build();
        log.info("Creating new channel group in server {}.", server.getId());
        channelGroup = channelGroupRepository.save(channelGroup);
        ChannelGroupCreatedListenerModel model = getCreatedModel(channelGroup);
        applicationEventPublisher.publishEvent(model);
        return channelGroup;
    }

    private ChannelGroupCreatedListenerModel getCreatedModel(AChannelGroup channelGroup) {
        return ChannelGroupCreatedListenerModel
                .builder()
                .channelGroupId(channelGroup.getId())
                .build();
    }

    private ChannelGroupDeletedListenerModel getDeletionModel(AChannelGroup channelGroup) {
        return ChannelGroupDeletedListenerModel
                .builder()
                .channelGroupId(channelGroup.getId())
                .build();
    }

    @Override
    public boolean doesChannelGroupExist(String name, AServer server) {
        return channelGroupRepository.existsByGroupNameIgnoreCaseAndServer(name, server);
    }

    @Override
    public void deleteChannelGroup(String name, AServer server) {
        name = name.toLowerCase();
        AChannelGroup existing = findByNameAndServer(name, server);
        if(existing == null) {
            throw new ChannelGroupNotFoundException(name, getAllAvailableAsString(server));
        }
        log.info("Deleting channel group {} in server {}.", existing.getId(), server.getId());
        // we need to execute the _sync_ listeners before actually deleting it, in order to allow others to delete their instance
        // this is a strong binding to listeners, might need to remove the direct connection at some point
        ChannelGroupDeletedListenerModel model = getDeletionModel(existing);
        deletedListenerManager.executeListener(model);
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
    public Optional<AChannelGroup> findChannelGroupByIdOptional(Long channelGroupId) {
        return channelGroupRepository.findById(channelGroupId);
    }

    @Override
    public AChannelGroup findChannelGroupById(Long channelGroupId) {
        return findChannelGroupByIdOptional(channelGroupId).orElseThrow(ChannelGroupNotFoundByIdException::new);
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
        String lowerCaseName = name.toLowerCase();
        return channelGroupRepository.findByGroupNameIgnoreCaseAndServer(lowerCaseName, server)
                .orElseThrow(() -> new ChannelGroupNotFoundException(name, getAllAvailableAsString(server)));
    }

    @Override
    public Optional<AChannelGroup> findByNameAndServerOptional(String name, AServer server) {
        return channelGroupRepository.findByGroupNameIgnoreCaseAndServer(name, server);
    }

    @Override
    public AChannelGroup findByNameAndServerAndType(String name, AServer server, String expectedType) {
        String lowerName = name.toLowerCase();
        Optional<AChannelGroup> channelOptional = channelGroupRepository.findByGroupNameAndServerAndChannelGroupType_GroupTypeKey(lowerName, server, expectedType);
        return channelOptional.orElseThrow(() -> {
            if(channelGroupRepository.existsByGroupNameIgnoreCaseAndServer(lowerName, server)) {
                return new ChannelGroupIncorrectTypeException(name.toLowerCase(), expectedType);
            } else {
                List<String> channelGroupNames = extractChannelGroupNames(findAllInServerWithType(server.getId(), expectedType));
                return new ChannelGroupNotFoundException(name.toLowerCase(), channelGroupNames);
            }
        });
    }

    @Override
    public List<AChannelGroup> findAllInServer(AServer server) {
        return channelGroupRepository.findByServer(server);
    }

    @Override
    public List<String> getAllAvailableAsString(AServer server) {
        List<AChannelGroup> allInServer = findAllInServer(server);
        return extractChannelGroupNames(allInServer);
    }

    private List<String> extractChannelGroupNames(List<AChannelGroup> allInServer) {
        return allInServer.stream().map(AChannelGroup::getGroupName).collect(Collectors.toList());
    }

    @Override
    public List<AChannelGroup> findAllInServer(Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return findAllInServer(server);
    }

    @Override
    public List<AChannelGroup> getAllChannelGroupsOfChannel(AChannel channel) {
        return channelGroupRepository.findAllByChannels(channel);
    }

    @Override
    public List<AChannelGroup> findAllInServerWithType(Long serverId, String type) {
        AServer server = serverManagementService.loadServer(serverId);
        return channelGroupRepository.findByServerAndChannelGroupType_GroupTypeKey(server, type);
    }

    @Override
    public boolean isChannelInEnabledChannelGroupOfType(String channelGroupType, Long channelId) {
        return channelGroupRepository.existsChannelInGroupOfType(channelGroupType, channelId);
    }
}
