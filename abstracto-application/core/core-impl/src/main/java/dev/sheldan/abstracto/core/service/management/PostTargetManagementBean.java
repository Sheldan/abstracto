package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.DynamicKeyLoader;
import dev.sheldan.abstracto.core.exception.PostTargetException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.repository.PostTargetRepository;
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

    @Autowired
    private DynamicKeyLoader dynamicKeyLoader;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void createPostTarget(String name, AServer server, AChannel targetChannel) {
        if(!postTargetService.validPostTarget(name)) {
            throw new PostTargetException("PostTarget not found. Possible values are: " + String.join(", ", dynamicKeyLoader.getPostTargetsAsList()));
        }
        log.info("Creating post target {} pointing towards {}", name, targetChannel);
        postTargetRepository.save(PostTarget.builder().name(name).channelReference(targetChannel).serverReference(server).build());
    }

    @Override
    public void createOrUpdate(String name, AServer server, AChannel targetChannel) {
        PostTarget existing = postTargetRepository.findPostTargetByNameAndServerReference(name, server);
        if(existing == null){
            this.createPostTarget(name, server, targetChannel);
        } else {
            this.updatePostTarget(existing, server, targetChannel);
        }
    }

    @Override
    public void createOrUpdate(String name, AServer server, Long channelId) {
        AChannel dbChannel = channelManagementService.loadChannel(channelId);
        createOrUpdate(name, server, dbChannel);
    }

    @Override
    public void createOrUpdate(String name, Long serverId, Long channelId) {
        AChannel dbChannel = channelManagementService.loadChannel(channelId);
        AServer dbServer = serverManagementService.loadOrCreate(serverId);
        createOrUpdate(name, dbServer, dbChannel);
    }

    @Override
    @Cacheable("posttargets")
    public PostTarget getPostTarget(String name, AServer server) {
        return postTargetRepository.findPostTargetByNameAndServerReference(name, server);
    }

    @Override
    public PostTarget getPostTarget(String name, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return getPostTarget(name, server);
    }

    @Override
    public void updatePostTarget(PostTarget target, AServer server, AChannel newTargetChannel) {
        postTargetRepository.getOne(target.getId()).setChannelReference(newTargetChannel);
    }

}