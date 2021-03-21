package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionRemovedListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.listener.ReactionRemovedModel;
import dev.sheldan.abstracto.starboard.config.StarboardFeature;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class StarRemovedListener extends StarboardListener implements AsyncReactionRemovedListener {

    protected static final CounterMetric STARBOARD_STARS_REMOVED = CounterMetric
            .builder()
            .name(STARBOARD_STARS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "removed")))
            .build();

    @Override
    @Transactional
    public DefaultListenerResult execute(ReactionRemovedModel model) {
        ServerUser userRemoving = model.getUserRemoving();
        if(model.getMessage().getAuthor().getAuthorId().equals(userRemoving.getUserId())) {
            return DefaultListenerResult.IGNORED;
        }
        Long guildId = model.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(StarboardFeature.STAR_EMOTE, guildId);
        if(emoteService.isReactionEmoteAEmote(model.getReaction().getReactionEmote(), aEmote)) {
            metricService.incrementCounter(STARBOARD_STARS_REMOVED);
            log.info("User {} in server {} removed star reaction from message {} on starboard.",
                    userRemoving.getUserId(), model.getServerId(), model.getMessage().getMessageId());
            Optional<CachedReactions> reactionOptional = emoteService.getReactionFromMessageByEmote(model.getMessage(), aEmote);
            handleStarboardPostChange(model.getMessage(), reactionOptional.orElse(null), userRemoving, false);
            return DefaultListenerResult.PROCESSED;
        } else {
            return DefaultListenerResult.IGNORED;
        }
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        metricService.registerCounter(STARBOARD_STARS_REMOVED, "Star reaction removed");
    }

    @Override
    public FeatureDefinition getFeature() {
        return StarboardFeatureDefinition.STARBOARD;
    }
}
