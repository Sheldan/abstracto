package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.listener.MessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.utility.config.UtilityFeatures;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StarboardPostDeletedListener implements MessageDeletedListener {

    @Autowired
    private StarboardPostManagementService starboardPostManagementService;

    @Override
    public void execute(CachedMessage messageBefore) {
        Optional<StarboardPost> byStarboardPostId = starboardPostManagementService.findByStarboardPostId(messageBefore.getMessageId());
        if(byStarboardPostId.isPresent()) {
            starboardPostManagementService.setStarboardPostIgnored(messageBefore.getMessageId(), true);
        }
    }

    @Override
    public String getFeature() {
        return UtilityFeatures.STARBOARD;
    }
}
