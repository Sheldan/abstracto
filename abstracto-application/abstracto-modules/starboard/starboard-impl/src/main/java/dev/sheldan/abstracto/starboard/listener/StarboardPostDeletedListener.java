package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureDefinition;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class StarboardPostDeletedListener implements AsyncMessageDeletedListener {

    @Autowired
    private StarboardPostManagementService starboardPostManagementService;

    @Override
    public void execute(CachedMessage messageBefore) {
        Optional<StarboardPost> byStarboardPostId = starboardPostManagementService.findByStarboardPostId(messageBefore.getMessageId());
        if(byStarboardPostId.isPresent()) {
            StarboardPost post = byStarboardPostId.get();
            log.info("Removing starboard post: message {}, channel {}, server {}, because the message was deleted",
                    post.getPostMessageId(), post.getSourceChannel().getId(), messageBefore.getServerId());
            starboardPostManagementService.setStarboardPostIgnored(messageBefore.getMessageId(), true);
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return StarboardFeatureDefinition.STARBOARD;
    }

}
