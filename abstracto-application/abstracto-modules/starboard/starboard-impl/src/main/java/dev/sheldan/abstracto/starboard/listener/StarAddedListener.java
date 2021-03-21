package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureConfig;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class StarAddedListener extends StarboardListener implements AsyncReactionAddedListener {

    protected static final CounterMetric STARBOARD_STARS_ADDED = CounterMetric
            .builder()
            .name(STARBOARD_STARS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "added")))
            .build();

    @Override
    public DefaultListenerResult execute(ReactionAddedModel model) {
        if(model.getUserReacting().getUserId().equals(model.getMessage().getAuthor().getAuthorId())) {
            return DefaultListenerResult.IGNORED;
        }
        Long serverId = model.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(StarboardFeatureConfig.STAR_EMOTE, serverId);
        if(emoteService.isReactionEmoteAEmote(model.getReaction().getReactionEmote(), aEmote)) {
            metricService.incrementCounter(STARBOARD_STARS_ADDED);
            log.info("User {} in server {} reacted with star to put a message {} from channel {} on starboard.",
                    model.getUserReacting().getUserId(), model.getServerId(), model.getMessage().getMessageId(), model.getMessage().getChannelId());
            Optional<CachedReactions> reactionOptional = emoteService.getReactionFromMessageByEmote(model.getMessage(), aEmote);
            handleStarboardPostChange(model.getMessage(), reactionOptional.orElse(null), model.getUserReacting(), true);
            return DefaultListenerResult.PROCESSED;
        } else {
            return DefaultListenerResult.IGNORED;
        }
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        metricService.registerCounter(STARBOARD_STARS_ADDED, "Star reaction added");
    }

    @Override
    public FeatureDefinition getFeature() {
        return StarboardFeatureDefinition.STARBOARD;
    }
}
