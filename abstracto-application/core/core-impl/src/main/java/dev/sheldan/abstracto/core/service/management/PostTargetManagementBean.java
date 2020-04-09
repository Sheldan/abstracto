package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.DynamicKeyLoader;
import dev.sheldan.abstracto.core.exception.PostTargetException;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.repository.PostTargetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PostTargetManagementBean {
    @Autowired
    private PostTargetRepository postTargetRepository;

    @Autowired
    private ChannelManagementServiceBean channelManagementService;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    @Autowired
    private DynamicKeyLoader dynamicKeyLoader;

    @Autowired
    private PostTargetService postTargetService;

    public void createPostTarget(String name, ServerDto server, ChannelDto targetChannel) {
        if(!postTargetService.validPostTarget(name)) {
            throw new PostTargetException("PostTarget not found. Possible values are: " + String.join(", ", dynamicKeyLoader.getPostTargetsAsList()));
        }
        log.info("Creating post target {} pointing towards {}", name, targetChannel);
        AChannel aChannel = AChannel.builder().id(targetChannel.getId()).build();
        AServer aServer = AServer.builder().id(server.getId()).build();
        PostTarget build = PostTarget
                .builder()
                .name(name)
                .channelReference(aChannel)
                .serverReference(aServer)
                .build();
        postTargetRepository.save(build);
    }

    public void createOrUpdate(String name, ServerDto server, ChannelDto targetChannel) {
        AServer aServer = AServer.builder().id(server.getId()).build();
        PostTarget existing = postTargetRepository.findPostTargetByNameAndServerReference(name, aServer);
        if(existing == null){
            this.createPostTarget(name, server, targetChannel);
        } else {
            this.updatePostTarget(existing, server, targetChannel);
        }
    }

    public void createOrUpdate(String name, ServerDto server, Long channelId) {
        ChannelDto channelDto = ChannelDto.builder().id(channelId).build();
        createOrUpdate(name, server, channelDto);
    }

    public void createOrUpdate(String name, Long serverId, Long channelId) {
        ChannelDto channelDto = ChannelDto.builder().id(channelId).build();
        ServerDto serverDto = ServerDto.builder().id(serverId).build();
        createOrUpdate(name, serverDto, channelDto);
    }

    public PostTarget getPostTarget(String name, ServerDto server) {
        AServer aServer = AServer.builder().id(server.getId()).build();
        return postTargetRepository.findPostTargetByNameAndServerReference(name, aServer);
    }

    public PostTarget getPostTarget(String name, Long serverId) {
        ServerDto serverDto = ServerDto.builder().id(serverId).build();
        return getPostTarget(name, serverDto);
    }

    public void updatePostTarget(PostTarget target, ServerDto server, ChannelDto newTargetChannel) {
        AChannel aChannel = AChannel.builder().id(newTargetChannel.getId()).build();
        postTargetRepository.getOne(target.getId()).setChannelReference(aChannel);
    }

}
