package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.commands.management.PostTargetException;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.repository.PostTargetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Slf4j
public class PostTargetServiceServiceBean implements PostTargetService {
    @Autowired
    private PostTargetRepository postTargetRepository;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerService serverService;

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
        AChannel dbChannel = channelService.loadChannel(channelId);
        createOrUpdate(name, dbChannel, server);
    }

    @Override
    public void createOrUpdate(String name, Long channelId, Long serverId) {
        AChannel dbChannel = channelService.loadChannel(channelId);
        AServer dbServer = serverService.loadServer(serverId);
        createOrUpdate(name, dbChannel, dbServer);
    }

    @Override
    @Cacheable("posttargets")
    public PostTarget getPostTarget(String name, AServer server) {
        return postTargetRepository.findPostTargetByName(name);
    }

    @Override
    public void updatePostTarget(PostTarget target, AChannel newTargetChannel, AServer server) {
        postTargetRepository.getOne(target.getId()).setChannelReference(newTargetChannel);
    }

}
