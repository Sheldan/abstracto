package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.commands.management.PostTargetException;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.repository.ChannelRepository;
import dev.sheldan.abstracto.repository.PostTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class PostTargetServiceServiceBean implements PostTargetService {
    @Autowired
    private PostTargetRepository postTargetRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Override
    @Transactional
    public void createPostTarget(String name, AChannel targetChannel) {
        if(!PostTarget.AVAILABLE_POST_TARGETS.contains(name)) {
            throw new PostTargetException("PostTarget not found");
        }
        postTargetRepository.save(PostTarget.builder().name(name).AChannel(targetChannel).build());
    }

    @Override
    @Transactional
    public void createOrUpdate(String name, AChannel targetChannel) {
        PostTarget existing = postTargetRepository.findPostTargetByName(name);
        if(existing == null){
            this.createPostTarget(name, targetChannel);
        } else {
            this.updatePostTarget(existing, targetChannel);
        }
    }

    @Override
    @Transactional
    public void updatePostTarget(PostTarget target, AChannel newTargetChannel) {
        postTargetRepository.getOne(target.getId()).setAChannel(newTargetChannel);
    }

}
