package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.PostTargetException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.PostTargetManagement;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.repository.PostTargetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PostTargetManagementBean implements PostTargetManagement {
    @Autowired
    private PostTargetRepository postTargetRepository;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public void createPostTarget(String name, AChannel targetChannel, AServer server) {
        if(!PostTarget.AVAILABLE_POST_TARGETS.contains(name)) {
            throw new PostTargetException("PostTarget not found");
        }
        log.info("Creating post target {} pointing towards {}", name, targetChannel);
        postTargetRepository.save(PostTarget.builder().name(name).channelReference(targetChannel).serverReference(server).build());
    }

    @Override
    public void createOrUpdate(String name, AChannel targetChannel, AServer server) {
        PostTarget existing = postTargetRepository.findPostTargetByName(name);
        if(existing == null){
            this.createPostTarget(name, targetChannel, server);
        } else {
            this.updatePostTarget(existing, targetChannel, server);
        }
    }

    @Override
    public void createOrUpdate(String name, Long channelId, AServer server) {
        AChannel dbChannel = channelManagementService.loadChannel(channelId);
        createOrUpdate(name, dbChannel, server);
    }

    @Override
    public void createOrUpdate(String name, Long channelId, Long serverId) {
        AChannel dbChannel = channelManagementService.loadChannel(channelId);
        AServer dbServer = serverManagementService.loadServer(serverId);
        createOrUpdate(name, dbChannel, dbServer);
    }

    @Override
    @Cacheable("posttargets")
    public PostTarget getPostTarget(String name, AServer server) {
        return postTargetRepository.findPostTargetByName(name);
    }

    @Override
    public PostTarget getPostTarget(String name, Long serverId) {
        AServer server = serverManagementService.loadServer(serverId);
        return getPostTarget(name, server);
    }

    @Override
    public void updatePostTarget(PostTarget target, AChannel newTargetChannel, AServer server) {
        postTargetRepository.getOne(target.getId()).setChannelReference(newTargetChannel);
    }

}
