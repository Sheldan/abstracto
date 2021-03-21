package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionClearedListener;
import dev.sheldan.abstracto.core.models.listener.ReactionClearedModel;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureDefinition;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class StarClearedListener extends StarboardListener implements AsyncReactionClearedListener {

    @Override
    public DefaultListenerResult execute(ReactionClearedModel model) {
        Optional<StarboardPost> starboardPostOptional = starboardPostManagementService.findByMessageId(model.getMessage().getMessageId());

        if(starboardPostOptional.isPresent()) {
            starboardPostOptional.ifPresent(starboardPost -> {
                log.info("Reactions on message {} in channel {} in server {} were cleared. Completely deleting the starboard post {}.",
                        model.getMessage().getMessageId(), model.getMessage().getChannelId(), model.getServerId(), starboardPost.getId());
                starboardPostReactorManagementService.removeReactors(starboardPost);
                completelyRemoveStarboardPost(starboardPost, null);
            });
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return StarboardFeatureDefinition.STARBOARD;
    }

}
