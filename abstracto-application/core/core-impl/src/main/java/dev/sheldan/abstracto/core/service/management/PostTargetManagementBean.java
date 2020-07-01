package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.exception.PostTargetNotValidException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.repository.PostTargetRepository;
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
    public PostTarget createPostTarget(String name, AServer server, AChannel targetChannel) {
        if(!postTargetService.validPostTarget(name)) {
            throw new PostTargetNotValidException(name, defaultPostTargetManagementService.getDefaultPostTargetKeys());
        }
        log.info("Creating post target {} pointing towards {}", name, targetChannel);
        PostTarget build = PostTarget.builder().name(name).channelReference(targetChannel).serverReference(server).build();
        postTargetRepository.save(build);
        return build;
    }

    @Override
    public PostTarget createOrUpdate(String name, AServer server, AChannel targetChannel) {
        PostTarget existing = postTargetRepository.findPostTargetByNameAndServerReference(name, server);
        if(existing == null){
            return this.createPostTarget(name, server, targetChannel);
        } else {
            return this.updatePostTarget(existing, server, targetChannel);
        }
    }

    @Override
    public PostTarget createOrUpdate(String name, AServer server, Long channelId) {
        Optional<AChannel> dbChannelOpt = channelManagementService.loadChannel(channelId);
        AChannel dbChannel = dbChannelOpt.orElseThrow(() -> new ChannelNotFoundException(channelId, server.getId()));
        return createOrUpdate(name, server, dbChannel);
    }

    @Override
    public PostTarget createOrUpdate(String name, Long serverId, Long channelId) {
        Optional<AChannel> dbChannelOpt = channelManagementService.loadChannel(channelId);
        AChannel dbChannel = dbChannelOpt.orElseThrow(() -> new ChannelNotFoundException(channelId, serverId));
        AServer dbServer = serverManagementService.loadOrCreate(serverId);
        return createOrUpdate(name, dbServer, dbChannel);
    }

    @Override
    public PostTarget getPostTarget(String name, AServer server) {
        return postTargetRepository.findPostTargetByNameAndServerReference(name, server);
    }

    @Override
    public PostTarget getPostTarget(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return getPostTarget(name, server);
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
    public PostTarget updatePostTarget(PostTarget target, AServer server, AChannel newTargetChannel) {
        target.setChannelReference(newTargetChannel);
        return target;
    }

    @Override
    public List<PostTarget> getPostTargetsInServer(AServer server) {
        return postTargetRepository.findByServerReference(server);
    }

}
