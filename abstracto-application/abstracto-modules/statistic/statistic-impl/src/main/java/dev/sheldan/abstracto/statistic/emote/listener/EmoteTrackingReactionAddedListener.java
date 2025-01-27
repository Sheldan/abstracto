package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmoteType;
import dev.sheldan.abstracto.statistic.emote.service.RunTimeReactionEmotesService;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmoteTrackingReactionAddedListener implements AsyncReactionAddedListener {

    @Autowired
    private RunTimeReactionEmotesService runTimeReactionEmotesService;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private EmoteService emoteService;

    @Override
    public DefaultListenerResult execute(ReactionAddedModel model) {
        if(model.getReaction().getEmoji().getType() != Emoji.Type.CUSTOM) {
            return DefaultListenerResult.IGNORED;
        }
        Long guildId = model.getServerId();
        Long messageId = model.getMessage().getMessageId();
        Long userId = model.getUserReacting().getUserId();
        CustomEmoji customEmoji = model.getReaction().getEmoji().asCustom();
        Long emoteId = customEmoji.getIdLong();
        if(runTimeReactionEmotesService.emoteAlreadyUsed(guildId, userId, messageId, emoteId)) {
            return DefaultListenerResult.IGNORED;
        }
        log.debug("Counting usage of emote {} in server {}.", emoteId, guildId);
        trackedEmoteService.addEmoteToRuntimeStorage(customEmoji, model.getReaction().getGuild(), 1L, UsedEmoteType.REACTION);
        runTimeReactionEmotesService.getRuntimeEmotes().put(runTimeReactionEmotesService.getKeyFormat(guildId, userId, messageId, emoteId), Instant.now());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(EmoteTrackingMode.TRACK_REACTIONS);
    }

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }

}
