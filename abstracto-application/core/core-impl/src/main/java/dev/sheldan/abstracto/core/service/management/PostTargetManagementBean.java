package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.exception.PostTargetNotFoundException;
import dev.sheldan.abstracto.core.exception.PostTargetNotValidException;
import dev.sheldan.abstracto.core.exception.ServerChannelConflictException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.repository.PostTargetRepository;
import dev.sheldan.abstracto.core.service.PostTargetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PostTargetManagementBean implements PostTargetManagement {

    @Autowired
    private PostTargetRepository postTargetRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private DefaultPostTargetManagementService defaultPostTargetManagementService;

    @Override
    public PostTarget createPostTarget(String name, AChannel targetChannel) {
        if(!postTargetService.validPostTarget(name)) {
            throw new PostTargetNotValidException(name, defaultPostTargetManagementService.getDefaultPostTargetKeys());
        }
        log.info("Creating post target {} pointing towards {} on server {}.", name, targetChannel.getId(), targetChannel.getServer().getId());
        PostTarget createdPostTarget = PostTarget
                .builder()
                .name(name)
                .channelReference(targetChannel)
                .serverReference(targetChannel.getServer())
                .disabled(false)
                .build();
        return postTargetRepository.save(createdPostTarget);
    }

    @Override
    public PostTarget createOrUpdate(String name, AChannel targetChannel) {
        Optional<PostTarget> existingTargetOptional = getPostTargetOptional(name, targetChannel.getServer().getId());
        if(!existingTargetOptional.isPresent()){
            return this.createPostTarget(name, targetChannel);
        } else {
            return this.updatePostTarget(existingTargetOptional.get(), targetChannel);
        }
    }

    @Override
    public PostTarget createOrUpdate(String name, AServer server, Long channelId) {
        AChannel dbChannel = channelManagementService.loadChannel(channelId);
        if(!dbChannel.getServer().getId().equals(server.getId())) {
            throw new ServerChannelConflictException(server.getId(), channelId);
        }
        return createOrUpdate(name, dbChannel);
    }

    @Override
    public PostTarget createOrUpdate(String name, Long serverId, Long channelId) {
        AChannel dbChannel = channelManagementService.loadChannel(channelId);
        if(!dbChannel.getServer().getId().equals(serverId)) {
            throw new ServerChannelConflictException(serverId, channelId);
        }
        return createOrUpdate(name, dbChannel);
    }

    @Override
    public PostTarget getPostTarget(String name, AServer server) {
        return postTargetRepository.findPostTargetByNameAndServerReference(name, server)
                .orElseThrow(() -> new PostTargetNotFoundException(name));
    }

    @Override
    public PostTarget getPostTarget(PostTargetEnum postTargetEnum, AServer server) {
        return getPostTarget(postTargetEnum.getKey(), server);
    }

    @Override
    public PostTarget getPostTarget(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return getPostTarget(name, server);
    }

    @Override
    public Optional<PostTarget> getPostTargetOptional(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return postTargetRepository.findPostTargetByNameAndServerReference(name, server);
    }

    @Override
    public Optional<PostTarget> getPostTargetOptional(PostTargetEnum postTargetEnum, Long serverId) {
        return getPostTargetOptional(postTargetEnum.getKey(), serverId);
    }

    @Override
    public Boolean postTargetExists(String name, AServer server) {
        return postTargetRepository.existsByNameAndServerReference(name, server);
    }

    @Override
    public boolean postTargetExists(String name, Long serverId) {
        AServer dbServer = serverManagementService.loadOrCreate(serverId);
        return postTargetExists(name, dbServer);
    }

    @Override
    public PostTarget updatePostTarget(PostTarget target, AChannel newTargetChannel) {
        target.setChannelReference(newTargetChannel);
        log.info("Setting post target {} pointing towards {} on server {}.", target.getName(), newTargetChannel.getId(), newTargetChannel.getServer().getId());
        return target;
    }

    @Override
    public List<PostTarget> getPostTargetsInServer(AServer server) {
        return postTargetRepository.findByServerReference(server);
    }

}
