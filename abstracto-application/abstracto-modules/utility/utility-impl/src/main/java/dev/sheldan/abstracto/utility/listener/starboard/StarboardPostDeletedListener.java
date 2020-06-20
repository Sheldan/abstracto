package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.MessageDeletedListener;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class StarboardPostDeletedListener implements MessageDeletedListener {

    @Autowired
    private StarboardPostManagementService starboardPostManagementService;

    @Override
    public void execute(CachedMessage messageBefore, AServerAChannelAUser authorUser, GuildChannelMember authorMember) {
        Optional<StarboardPost> byStarboardPostId = starboardPostManagementService.findByStarboardPostId(messageBefore.getMessageId());
        if(byStarboardPostId.isPresent()) {
            StarboardPost post = byStarboardPostId.get();
            log.info("Removing starboard post: message {}, channel {}, server {}, because the message was deleted",
                    post.getPostMessageId(), post.getSourceChanel().getId(), post.getAuthor().getUserReference().getId());
            starboardPostManagementService.setStarboardPostIgnored(messageBefore.getMessageId(), true);
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.STARBOARD;
    }
}
